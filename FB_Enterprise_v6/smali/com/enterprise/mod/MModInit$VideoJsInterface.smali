.class public Lcom/enterprise/mod/MModInit$VideoJsInterface;
.super Ljava/lang/Object;
.source "ModInit.java"
.enclosing class Lcom/enterprise/mod/ModInit;

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# ModInit inner class – JS Interface for video download trigger from WebView JS

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public triggerDownload(Ljava/lang/String;Ljava/lang/String;)V
    .locals 2
    invoke-static {}, Lcom/enterprise/mod/ModInit;->getInstance()Lcom/enterprise/mod/ModInit;
    move-result-object v0
    iget-object v1, v0, Lcom/enterprise/mod/ModInit;->l8:Lcom/enterprise/mod/L8_VideoDownloader;
    invoke-virtual {v1, p1, p2}, Lcom/enterprise/mod/L8_VideoDownloader;->downloadVideo(Ljava/lang/String;Ljava/lang/String;)J
    return-void
.end method
