# Plex Playlist Parser
I happen to own a car that lets me plug in a USB drive full of music. I had found that I was often too lazy to update the drive because I didn't feel like looking at my existing Plex playlists, finding each album individually, then copying them off to some other directory before converting FLAC files to MP3 and then zipping everything up. This script was born to automate the whole process for me.

## Running the Script
```
groovy parser.groovy -h <hostname> -l <playlist> -t <token> [options]
```

## Options
- `-help` - Lists the app's help
- `-c`, `--convert` - Convert FLAC files to 320 kbps MP3 files.
- `-h`, `--host` - The hostname for your Plex server. Required.
- `-l`, `--playlist` - The name of the playlist you'd like to parse. Required.
- `-o`, `--output` - The output directory to copy your files into. If unset, uses the directory where you are running the script.
- `-p` , `--port` - The port for your Plex server. Defaults to 32400.
- `-t`, `--token` - The Plex token required to access the API. See [Plex Support](https://support.plex.tv/hc/en-us/articles/204059436) for information on how to find your token. Required.
- `-z`, `--zip` - Zip up the copied msuic files and delete the output directory.

## Assumptions
- You are running the script on the same machine as your Plex server
- You store all of your music in Artist/Album/Track.filetype format
- ~~You want to convert your FLAC files to MP3~~
  - This is now an option determined by the `-c` flag
- You want your MP3s to be 320 kbps (if you convert from FLAC)
- You want to delete the files you copied when you Zip everything up
  - Zipping files is controlled by the `-z` flag
