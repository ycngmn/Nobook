.class public Lcom/enterprise/mod/ModApplication;
.super Landroid/app/Application;
.source "ModApplication.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# ModApplication – Replaces KatanaApplication as Application class entry point

.method public onCreate()V
    .locals 2
    invoke-super {p0}, Landroid/app/Application;->onCreate()V
    # Bootstrap ModInit on app startup
    invoke-static {}, Lcom/enterprise/mod/ModInit;->getInstance()Lcom/enterprise/mod/ModInit;
    move-result-object v0
    invoke-virtual {p0}, Landroid/app/Application;->getApplicationContext()Landroid/content/Context;
    move-result-object v1
    invoke-virtual {v0, v1}, Lcom/enterprise/mod/ModInit;->init(Landroid/content/Context;)V
    const-string v0, "ModApplication"
    const-string v1, "FB Enterprise AdBlocker v8.0 – ModApplication started"
    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    return-void
.end method
