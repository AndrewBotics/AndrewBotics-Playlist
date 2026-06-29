import os
import yt_dlp

def download_wav(youtube_url):
    ydl_opts = {
        'format': 'bestaudio/best',
        'postprocessors': [{
            'key': 'FFmpegExtractAudio',
            'preferredcodec': 'wav',
        }],
        'outtmpl': 'grabber\\Songs\\%(title)s.%(ext)s', 
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([youtube_url])

def download_png(youtube_url):
    ydl_opts = {
        'writethumbnail': True,
        'skip_download': True,
        'postprocessors': [{
            'key': 'FFmpegThumbnailsConvertor',
            'format': 'png'
        }],
        'outtmpl': 'grabber\\Images\\%(title)s.%(ext)s',
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([youtube_url])

video_links = [
    "https://youtu.be/qsIpeH_eWx8?si=zVnUPGJ9wuwS5sr3",
    "https://youtu.be/a2awUdeorb8?si=IjG_m6iJmeIbl4Bq",
    "https://youtu.be/LvYL8u4p-aM?si=Gy7w1Xi6TPMvJUuw",
    ]
for video in video_links:
    download_wav(video)
    download_png(video)