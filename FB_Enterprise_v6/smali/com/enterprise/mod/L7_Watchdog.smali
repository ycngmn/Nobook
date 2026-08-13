.class public Lcom/enterprise/mod/L7_Watchdog;
.super Ljava/lang/Object;
.source "L7_Watchdog.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# Layer 7 – Watchdog: Periodic health check, auto-reinject if hooks dropped

.implements Ljava/lang/Runnable;

.field private static final TAG:Ljava/lang/String; = "L7_Watchdog"
.field private static final VERSION:Ljava/lang/String; = "8.0-Enterprise-FINAL"
.field private static final INTERVAL_MS:J = 30000L
.field private handler:Landroid/os/Handler;
.field private running:Z
.field private checkCount:I
.field private lastCheck:J

.method public constructor <init>()V
    .locals 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    new-instance v0, Landroid/os/Handler;
    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
    move-result-object v0
    new-instance v1, Landroid/os/Handler;
    invoke-direct {v1, v0}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
    iput-object v1, p0, Lcom/enterprise/mod/L7_Watchdog;->handler:Landroid/os/Handler;
    return-void
.end method

.method public start()V
    .locals 2
    const/4 v0, 0x1
    iput-boolean v0, p0, Lcom/enterprise/mod/L7_Watchdog;->running:Z
    iget-object v1, p0, Lcom/enterprise/mod/L7_Watchdog;->handler:Landroid/os/Handler;
    sget-wide v0, Lcom/enterprise/mod/L7_Watchdog;->INTERVAL_MS:J
    invoke-virtual {v1, p0, v0, v0}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z
    return-void
.end method

.method public stop()V
    .locals 1
    const/4 v0, 0x0
    iput-boolean v0, p0, Lcom/enterprise/mod/L7_Watchdog;->running:Z
    iget-object v0, p0, Lcom/enterprise/mod/L7_Watchdog;->handler:Landroid/os/Handler;
    invoke-virtual {v0, p0}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V
    return-void
.end method

.method public run()V
    .locals 3
    iget-boolean v0, p0, Lcom/enterprise/mod/L7_Watchdog;->running:Z
    if-eqz v0, :exit
    # Increment check count
    iget v1, p0, Lcom/enterprise/mod/L7_Watchdog;->checkCount:I
    add-int/lit8 v1, v1, 0x1
    iput v1, p0, Lcom/enterprise/mod/L7_Watchdog;->checkCount:I
    # Record last check timestamp
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v2
    iput-wide v2, p0, Lcom/enterprise/mod/L7_Watchdog;->lastCheck:J
    # Verify all layers are active via ModInit
    invoke-static {}, Lcom/enterprise/mod/ModInit;->getInstance()Lcom/enterprise/mod/ModInit;
    move-result-object v0
    invoke-virtual {v0}, Lcom/enterprise/mod/ModInit;->areAllLayersActive()Z
    move-result v1
    if-nez v1, :reschedule
    invoke-virtual {v0}, Lcom/enterprise/mod/ModInit;->reinjectAllLayers()V
    :reschedule
    iget-object v0, p0, Lcom/enterprise/mod/L7_Watchdog;->handler:Landroid/os/Handler;
    sget-wide v1, Lcom/enterprise/mod/L7_Watchdog;->INTERVAL_MS:J
    invoke-virtual {v0, p0, v1, v1}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z
    :exit
    return-void
.end method
