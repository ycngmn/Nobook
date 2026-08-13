.class public Lcom/enterprise/mod/L13_VirtualCamera;
.super Ljava/lang/Object;
.source "L13_VirtualCamera.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# Layer 13 – Virtual Camera: Feed custom video frames to FB camera API

.field private static final TAG:Ljava/lang/String; = "L13_VirtualCamera"
.field private static final VERSION:Ljava/lang/String; = "8.0-Enterprise-FINAL"
.field private context:Landroid/content/Context;
.field private surface:Landroid/view/Surface;
.field private mediaPlayer:Landroid/media/MediaPlayer;
.field private prepared:Z
.field private errorListener:Lcom/enterprise/mod/L13_VirtualCamera$ErrorListener;
.field private preparedListener:Lcom/enterprise/mod/L13_VirtualCamera$PreparedListener;

.method public constructor <init>(Landroid/content/Context;)V
    .locals 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/enterprise/mod/L13_VirtualCamera;->context:Landroid/content/Context;
    const/4 v0, 0x0
    iput-boolean v0, p0, Lcom/enterprise/mod/L13_VirtualCamera;->prepared:Z
    return-void
.end method

.method public initWithFile(Ljava/lang/String;)V
    .locals 2
    new-instance v0, Landroid/media/MediaPlayer;
    invoke-direct {v0}, Landroid/media/MediaPlayer;-><init>()V
    iput-object v0, p0, Lcom/enterprise/mod/L13_VirtualCamera;->mediaPlayer:Landroid/media/MediaPlayer;
    invoke-virtual {v0, p1}, Landroid/media/MediaPlayer;->setDataSource(Ljava/lang/String;)V
    iget-object v1, p0, Lcom/enterprise/mod/L13_VirtualCamera;->preparedListener:Lcom/enterprise/mod/L13_VirtualCamera$PreparedListener;
    invoke-virtual {v0, v1}, Landroid/media/MediaPlayer;->setOnPreparedListener(Landroid/media/MediaPlayer$OnPreparedListener;)V
    iget-object v1, p0, Lcom/enterprise/mod/L13_VirtualCamera;->errorListener:Lcom/enterprise/mod/L13_VirtualCamera$ErrorListener;
    invoke-virtual {v0, v1}, Landroid/media/MediaPlayer;->setOnErrorListener(Landroid/media/MediaPlayer$OnErrorListener;)V
    invoke-virtual {v0}, Landroid/media/MediaPlayer;->prepareAsync()V
    return-void
.end method

.method public release()V
    .locals 1
    iget-object v0, p0, Lcom/enterprise/mod/L13_VirtualCamera;->mediaPlayer:Landroid/media/MediaPlayer;
    if-eqz v0, :skip
    invoke-virtual {v0}, Landroid/media/MediaPlayer;->release()V
    const-null v0
    iput-object v0, p0, Lcom/enterprise/mod/L13_VirtualCamera;->mediaPlayer:Landroid/media/MediaPlayer;
    :skip
    return-void
.end method
