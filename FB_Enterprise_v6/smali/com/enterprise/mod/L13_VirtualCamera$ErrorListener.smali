.class public Lcom/enterprise/mod/L13_VirtualCamera$ErrorListener;
.super Ljava/lang/Object;
.source "L13_VirtualCamera.java"
.enclosing class Lcom/enterprise/mod/L13_VirtualCamera;

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# L13_VirtualCamera inner class – ErrorListener

.implements Landroid/media/MediaPlayer$OnErrorListener;

.method public onError(Landroid/media/MediaPlayer;II)Z
    .locals 2
    const-string v0, "L13_VirtualCamera"
    const-string v1, "VirtualCamera error – reinitializing..."
    invoke-static {v0, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    const/4 v0, 0x1
    return v0
.end method
