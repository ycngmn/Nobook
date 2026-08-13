.class public Lcom/enterprise/mod/L1_AdBlockEngine;
.super Ljava/lang/Object;
.source "L1_AdBlockEngine.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# Layer 1 – Core Ad Block Engine
# Intercepts WebView loadUrl / shouldOverrideUrlLoading
# Applies URL pattern matching against guard_v9.2 ruleset

.implements Landroid/webkit/WebViewClient;

.field private static final TAG:Ljava/lang/String; = "L1_AdBlockEngine"
.field private static final VERSION:Ljava/lang/String; = "8.0-Enterprise-FINAL"
.field private static volatile instance:Lcom/enterprise/mod/L1_AdBlockEngine;
.field private adPatterns:Ljava/util/List;
.field private statsBlocked:I
.field private statsAllowed:I
.field private enabled:Z
.field private context:Landroid/content/Context;

.method public static getInstance(Landroid/content/Context;)Lcom/enterprise/mod/L1_AdBlockEngine;
    .locals 2
    sget-object v0, Lcom/enterprise/mod/L1_AdBlockEngine;->instance:Lcom/enterprise/mod/L1_AdBlockEngine;
    if-nez v0, :return
    :sync_start
    sget-object v0, Lcom/enterprise/mod/L1_AdBlockEngine;->instance:Lcom/enterprise/mod/L1_AdBlockEngine;
    if-nez v0, :return
    new-instance v0, Lcom/enterprise/mod/L1_AdBlockEngine;
    invoke-direct {v0, p0}, Lcom/enterprise/mod/L1_AdBlockEngine;-><init>(Landroid/content/Context;)V
    sput-object v0, Lcom/enterprise/mod/L1_AdBlockEngine;->instance:Lcom/enterprise/mod/L1_AdBlockEngine;
    :return
    sget-object v0, Lcom/enterprise/mod/L1_AdBlockEngine;->instance:Lcom/enterprise/mod/L1_AdBlockEngine;
    return-object v0
.end method

.method private constructor <init>(Landroid/content/Context;)V
    .locals 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->context:Landroid/content/Context;
    const/4 v0, 0x1
    iput-boolean v0, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->enabled:Z
    const/4 v1, 0x0
    iput v1, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->statsBlocked:I
    iput v1, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->statsAllowed:I
    invoke-virtual {p0}, Lcom/enterprise/mod/L1_AdBlockEngine;->loadPatterns()V
    return-void
.end method

.method private loadPatterns()V
    .locals 2
    new-instance v0, Ljava/util/ArrayList;
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
    iput-object v0, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->adPatterns:Ljava/util/List;
    # Pattern group: Facebook ad tracking domains
    const-string v1, "an\.facebook\.com"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    const-string v1, "graph\.facebook\.com/v[0-9]+/[0-9]+/activities"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    const-string v1, "www\.facebook\.com/ajax/bz"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    const-string v1, "connect\.facebook\.net/en_US/fbevents\.js"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    const-string v1, "static\.xx\.fbcdn\.net.*sponsored"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    const-string v1, "facebook\.com/ads/.*"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    const-string v1, "facebook\.com/adview"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    const-string v1, "pixel\.facebook\.com"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    const-string v1, "www\.facebook\.com/audience_network"
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    return-void
.end method

.method public shouldBlockUrl(Ljava/lang/String;)Z
    .locals 4
    if-nez p0, :check_enabled
    :block_null
    const/4 v0, 0x0
    return v0
    :check_enabled
    iget-boolean v0, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->enabled:Z
    if-nez v0, :check_patterns
    const/4 v0, 0x0
    return v0
    :check_patterns
    iget-object v1, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->adPatterns:Ljava/util/List;
    invoke-interface {v1}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v2
    :loop
    invoke-interface {v2}, Ljava/util/Iterator;->hasNext()Z
    move-result v3
    if-eqz v3, :no_match
    invoke-interface {v2}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v3
    check-cast v3, Ljava/lang/String;
    invoke-virtual {p1, v3}, Ljava/lang/String;->matches(Ljava/lang/String;)Z
    move-result v3
    if-eqz v3, :loop
    :matched
    iget v3, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->statsBlocked:I
    add-int/lit8 v3, v3, 0x1
    iput v3, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->statsBlocked:I
    const/4 v0, 0x1
    return v0
    :no_match
    iget v3, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->statsAllowed:I
    add-int/lit8 v3, v3, 0x1
    iput v3, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->statsAllowed:I
    const/4 v0, 0x0
    return v0
.end method

.method public getStats()Ljava/lang/String;
    .locals 3
    new-instance v0, Ljava/lang/StringBuilder;
    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
    const-string v1, "L1_AdBlockEngine v8.0 | Blocked: "
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    iget v2, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->statsBlocked:I
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
    const-string v1, " | Allowed: "
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    iget v2, p0, Lcom/enterprise/mod/L1_AdBlockEngine;->statsAllowed:I
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method
