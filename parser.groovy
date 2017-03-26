cli = new CliBuilder(
    usage: 'plex -h hostname -p port -t token [options]',
    header: '\nAvailable options (use -help for help):\n',
    footer: '\nNote: This application assumes that you have the ffmpeg command line tool installed.\n'
)
cli.help('Print this message')
cli.c(longOpt: 'convert', 'Convert FLAC files to 320kbps MP3 files.')
cli.h(args: 1, longOpt: 'host', 'The hostname for your Plex server', required: true)
cli.l(args: 1, longOpt: 'playlist', 'The playlist to parse', required: true)
cli.o(args: 1, longOpt: 'output', 'The output directory to use when copying/converting files')
cli.p(args: 1, longOpt: 'port', 'The port for your Plex server. Defaults to 32400')
cli.t(args: 1, longOpt: 'token', 'The Plex token required to access the API. See https://support.plex.tv/hc/en-us/articles/204059436', required: true)
cli.z(longOpt: 'zip', 'Zip up the copied music files and delete the output directory')

def options = cli.parse(args)
if (!options) {
    return
}
else if (options.help) {
    cli.usage()
}

def outputDir = new File(options.o ?: '.')
outputDir.mkdirs()

def playlistName = options.l

// Normalize the URL and add the port
def plexServerUrl = options.h
if (!plexServerUrl.startsWith('http')) {
    plexServerUrl = "http://${plexServerUrl}"
}
plexServerUrl += ":${options.p ?: '32400'}"

// Create the token query parameter
def token = "?X-Plex-Token=${options.t}"

def playlistList = "${plexServerUrl}/playlists${token}".toURL().text
def playlistListSlurper = new XmlSlurper().parseText(playlistList)

def playlist = playlistListSlurper.Playlist.find { it.@title == playlistName }
def playlistItems = "${plexServerUrl}${playlist.@key}${token}".toURL().text
playlistItemsSlurper = new XmlSlurper().parseText(playlistItems)

playlistItemsSlurper.Track.each { track ->
    File trackFile = new File(track.Media.Part.@file.toString())
    def parents = "${trackFile.parentFile.parentFile.name}/${trackFile.parentFile.name}"
    
    // Create the new destination file
    def destDir = new File(outputDir, parents)
    destDir.mkdirs()
    destFile = new File(destDir, trackFile.name)
    
    // Copy the file to the new directory
    srcStream = trackFile.newDataInputStream()
    destStream = destFile.newDataOutputStream()
    destStream << srcStream
    srcStream.close()
    destStream.close()
}

if (options.c) {
    File script = new File(outputDir, "converter.sh")
    script.text = '''find -name "*.flac" -exec bash -c 'ffmpeg -i "{}" -y -acodec libmp3lame -ab 320k "${0/.flac}.mp3"' {} \\;
    find . -name "*.flac" -exec rm {} \\;
    '''
    executeOnShell('chmod +x converter.sh', outputDir.absoluteFile)
    executeOnShell('sh converter.sh', outputDir.absoluteFile)
    executeOnShell('rm converter.sh', outputDir.absoluteFile)
}

if (options.z) {
    executeOnShell("zip -r ${outputDir.name} *", outputDir.absoluteFile)
    executeOnShell("mv ${outputDir.absoluteFile}/${outputDir.name}.zip .")
    outputDir.deleteDir()
}

def executeOnShell(String command) {
  return executeOnShell(command, new File(System.properties.'user.dir'))
}
 
private def executeOnShell(String command, File workingDir) {
  println command
  def process = new ProcessBuilder(addShellPrefix(command))
                                    .directory(workingDir)
                                    .redirectErrorStream(true)
                                    .start()
  process.inputStream.eachLine { println it }
  process.waitFor();
  return process.exitValue()
}
 
private def addShellPrefix(String command) {
  commandArray = new String[3]
  commandArray[0] = "sh"
  commandArray[1] = "-c"
  commandArray[2] = command
  return commandArray
}
