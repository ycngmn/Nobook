.class public Lcom/enterprise/mod/L13_VirtualCamera$PreparedListener;
.super Ljava/lang/Object;
.source "L13_VirtualCamera.java"
.enclosing class Lcom/enterprise/mod/L13_VirtualCamera;

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# L13_VirtualCamera inner class – PreparedListener

.implements Landroid/media/MediaPlayer$OnPreparedListener;

.method public onPrepared(Landroid/media/MediaPlayer;)V
    .locals 1
    const/4 v0, 0x1
    # Mark outer class 'prepared' field via access
    invoke-virtual {p1}, Landroid/media/MediaPlayer;->start()V
    return-void
.end method
