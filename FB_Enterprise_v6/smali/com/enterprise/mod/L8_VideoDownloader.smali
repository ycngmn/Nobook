.class public Lcom/enterprise/mod/L8_VideoDownloader;
.super Ljava/lang/Object;
.source "L8_VideoDownloader.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# Layer 8 – Video Downloader: Intercept .mp4 stream URLs, trigger download

.field private static final TAG:Ljava/lang/String; = "L8_VideoDownloader"
.field private static final VERSION:Ljava/lang/String; = "8.0-Enterprise-FINAL"
.field private context:Landroid/content/Context;
.field private downloadManager:Landroid/app/DownloadManager;
.field private pendingUrls:Ljava/util/Queue;

.method public constructor <init>(Landroid/content/Context;)V
    .locals 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/enterprise/mod/L8_VideoDownloader;->context:Landroid/content/Context;
    const-string v0, "download"
    invoke-virtual {p1, v0}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Landroid/app/DownloadManager;
    iput-object v0, p0, Lcom/enterprise/mod/L8_VideoDownloader;->downloadManager:Landroid/app/DownloadManager;
    new-instance v1, Ljava/util/LinkedList;
    invoke-direct {v1}, Ljava/util/LinkedList;-><init>()V
    iput-object v1, p0, Lcom/enterprise/mod/L8_VideoDownloader;->pendingUrls:Ljava/util/Queue;
    return-void
.end method

.method public interceptVideoUrl(Ljava/lang/String;)Z
    .locals 3
    if-eqz p1, :not_video
    const-string v0, ".mp4"
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v1
    if-nez v1, :is_video
    const-string v0, "video_redirect"
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v1
    if-nez v1, :is_video
    :not_video
    const/4 v0, 0x0
    return v0
    :is_video
    iget-object v0, p0, Lcom/enterprise/mod/L8_VideoDownloader;->pendingUrls:Ljava/util/Queue;
    invoke-interface {v0, p1}, Ljava/util/Queue;->offer(Ljava/lang/Object;)Z
    invoke-virtual {p0, p1}, Lcom/enterprise/mod/L8_VideoDownloader;->showDownloadPrompt(Ljava/lang/String;)V
    const/4 v0, 0x1
    return v0
.end method

.method private showDownloadPrompt(Ljava/lang/String;)V
    .locals 4
    iget-object v0, p0, Lcom/enterprise/mod/L8_VideoDownloader;->context:Landroid/content/Context;
    new-instance v1, Landroid/app/AlertDialog$Builder;
    invoke-direct {v1, v0}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V
    const-string v2, "Download Video"
    invoke-virtual {v1, v2}, Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/String;)Landroid/app/AlertDialog$Builder;
    const-string v2, "Save this video to your device?"
    invoke-virtual {v1, v2}, Landroid/app/AlertDialog$Builder;->setMessage(Ljava/lang/String;)Landroid/app/AlertDialog$Builder;
    invoke-virtual {v1}, Landroid/app/AlertDialog$Builder;->create()Landroid/app/AlertDialog;
    move-result-object v3
    invoke-virtual {v3}, Landroid/app/AlertDialog;->show()V
    return-void
.end method

.method public downloadVideo(Ljava/lang/String;Ljava/lang/String;)J
    .locals 4
    invoke-static {p1}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;
    move-result-object v0
    new-instance v1, Landroid/app/DownloadManager$Request;
    invoke-direct {v1, v0}, Landroid/app/DownloadManager$Request;-><init>(Landroid/net/Uri;)V
    const-string v2, "video/mp4"
    invoke-virtual {v1, v2}, Landroid/app/DownloadManager$Request;->setMimeType(Ljava/lang/String;)Landroid/app/DownloadManager$Request;
    const/4 v2, 0x1
    invoke-virtual {v1, v2}, Landroid/app/DownloadManager$Request;->setAllowedOverRoaming(Z)Landroid/app/DownloadManager$Request;
    const/4 v2, 0x1
    invoke-virtual {v1, v2}, Landroid/app/DownloadManager$Request;->setNotificationVisibility(I)Landroid/app/DownloadManager$Request;
    const-string v2, "Movies"
    invoke-virtual {v1, p2, v2}, Landroid/app/DownloadManager$Request;->setDestinationInExternalPublicDir(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager$Request;
    iget-object v2, p0, Lcom/enterprise/mod/L8_VideoDownloader;->downloadManager:Landroid/app/DownloadManager;
    invoke-virtual {v2, v1}, Landroid/app/DownloadManager;->enqueue(Landroid/app/DownloadManager$Request;)J
    move-result-wide v3
    return-wide v3
.end method
