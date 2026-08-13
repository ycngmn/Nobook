.class public Lcom/enterprise/mod/L5_UrlGuard;
.super Ljava/lang/Object;
.source "L5_UrlGuard.java"

# FB Enterprise AdBlocker v7.0 Enterprise FINAL
# Layer 5 – URL Guard: Redirect & tracking-param stripper

.field private static final TAG:Ljava/lang/String; = "L5_UrlGuard"
.field private static final VERSION:Ljava/lang/String; = "7.0-Enterprise-FINAL"

.method public sanitizeUrl(Ljava/lang/String;)Ljava/lang/String;
    .locals 4
    if-eqz p1, :return_original
    invoke-static {p1}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;
    move-result-object v0
    # Remove tracking query params: fbclid, utm_*, ad_id, etc.
    new-instance v1, Landroid/net/Uri$Builder;
    invoke-direct {v1}, Landroid/net/Uri$Builder;-><init>()V
    invoke-virtual {v0}, Landroid/net/Uri;->getScheme()Ljava/lang/String;
    move-result-object v2
    invoke-virtual {v1, v2}, Landroid/net/Uri$Builder;->scheme(Ljava/lang/String;)Landroid/net/Uri$Builder;
    invoke-virtual {v0}, Landroid/net/Uri;->getHost()Ljava/lang/String;
    move-result-object v2
    invoke-virtual {v1, v2}, Landroid/net/Uri$Builder;->authority(Ljava/lang/String;)Landroid/net/Uri$Builder;
    invoke-virtual {v0}, Landroid/net/Uri;->getPath()Ljava/lang/String;
    move-result-object v2
    invoke-virtual {v1, v2}, Landroid/net/Uri$Builder;->path(Ljava/lang/String;)Landroid/net/Uri$Builder;
    invoke-virtual {v0}, Landroid/net/Uri;->getQueryParameterNames()Ljava/util/Set;
    move-result-object v2
    invoke-interface {v2}, Ljava/util/Set;->iterator()Ljava/util/Iterator;
    move-result-object v3
    :loop
    invoke-interface {v3}, Ljava/util/Iterator;->hasNext()Z
    move-result v0
    if-eqz v0, :done
    invoke-interface {v3}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/lang/String;
    invoke-virtual {p0, v0}, Lcom/enterprise/mod/L5_UrlGuard;->isTrackingParam(Ljava/lang/String;)Z
    move-result-object v0
    if-nez v0, :loop
    invoke-virtual {v0}, Landroid/net/Uri;->getQueryParameter(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v2
    invoke-virtual {v1, v0, v2}, Landroid/net/Uri$Builder;->appendQueryParameter(Ljava/lang/String;Ljava/lang/String;)Landroid/net/Uri$Builder;
    goto :loop
    :done
    invoke-virtual {v1}, Landroid/net/Uri$Builder;->build()Landroid/net/Uri;
    move-result-object v0
    invoke-virtual {v0}, Landroid/net/Uri;->toString()Ljava/lang/String;
    move-result-object v0
    return-object v0
    :return_original
    return-object p1
.end method

.method private isTrackingParam(Ljava/lang/String;)Z
    .locals 2
    const-string v0, "fbclid"
    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-nez v1, :yes
    const-string v0, "ad_id"
    invoke-virtual {p1, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v1
    if-nez v1, :yes
    const-string v0, "utm_"
    invoke-virtual {p1, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v1
    if-nez v1, :yes
    const-string v0, "hrc"
    invoke-virtual {p1, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v1
    if-nez v1, :yes
    const/4 v1, 0x0
    return v1
    :yes
    const/4 v1, 0x1
    return v1
.end method
