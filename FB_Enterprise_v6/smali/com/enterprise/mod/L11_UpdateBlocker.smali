.class public Lcom/enterprise/mod/L11_UpdateBlocker;
.super Ljava/lang/Object;
.source "L11_UpdateBlocker.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# Layer 11 – Update Blocker: Intercept in-app update checks, spoof version response

.field private static final TAG:Ljava/lang/String; = "L11_UpdateBlocker"
.field private static final VERSION:Ljava/lang/String; = "8.0-Enterprise-FINAL"
.field private static final SPOOF_VERSION:Ljava/lang/String; = "999.0.0.0.0"

.method public shouldInterceptUpdateCheck(Ljava/lang/String;)Z
    .locals 2
    if-eqz p1, :no
    const-string v0, "update_check"
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v1
    if-nez v1, :yes
    const-string v0, "app_version_check"
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v1
    if-nez v1, :yes
    const-string v0, "play_store_version"
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v1
    if-nez v1, :yes
    :no
    const/4 v0, 0x0
    return v0
    :yes
    const/4 v0, 0x1
    return v0
.end method

.method public getSpoofResponse()Ljava/lang/String;
    .locals 1
    const-string v0, "{\"update_available\":false,\"latest_version\":\"999.0.0.0.0\",\"force_update\":false}"
    return-object v0
.end method
