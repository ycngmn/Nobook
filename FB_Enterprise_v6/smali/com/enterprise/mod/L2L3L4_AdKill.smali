.class public Lcom/enterprise/mod/L2L3L4_AdKill;
.super Ljava/lang/Object;
.source "L2L3L4_AdKill.java"

# FB Enterprise AdBlocker v7.0 Enterprise FINAL
# Layer 2/3/4 – Multi-Layer Ad Kill
# L2: DOM element removal via JS injection
# L3: OkHttp/Volley interceptor
# L4: Native GraphQL ad payload filter

.field public static final TAG:Ljava/lang/String; = "L2L3L4_AdKill"
.field private static final VERSION:Ljava/lang/String; = "7.0-Enterprise-FINAL"
.field private webView:Landroid/webkit/WebView;
.field private httpClient:Ljava/lang/Object;
.field private isL2Active:Z
.field private isL3Active:Z
.field private isL4Active:Z

# ── L2: DOM Ad Node Removal ──────────────────
.method public applyL2DomKill(Landroid/webkit/WebView;)V
    .locals 3
    iput-object p1, p0, Lcom/enterprise/mod/L2L3L4_AdKill;->webView:Landroid/webkit/WebView;
    # JS snippet removes sponsored/ad containers from DOM
    const-string v0, "javascript:(function(){var ads=['[data-pagelet*=\"FeedUnit\"]','[aria-label=\"Sponsored\"]','[data-adunit]','[id*=\"ads\"]','[class*=\"sponsored\"]','[data-testid=\"story-sponsored-label\"]'];ads.forEach(function(sel){document.querySelectorAll(sel).forEach(function(el){el.remove();});});})();"
    if-eqz p1, :skip_l2
    invoke-virtual {p1, v0}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V
    const/4 v1, 0x1
    iput-boolean v1, p0, Lcom/enterprise/mod/L2L3L4_AdKill;->isL2Active:Z
    :skip_l2
    return-void
.end method

# ── L3: Network Request Interceptor ──────────────────
.method public shouldInterceptRequest(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;
    .locals 5
    invoke-interface {p2}, Landroid/webkit/WebResourceRequest;->getUrl()Landroid/net/Uri;
    move-result-object v0
    invoke-virtual {v0}, Landroid/net/Uri;->toString()Ljava/lang/String;
    move-result-object v1
    # Check for ad-related endpoints
    const-string v2, "sponsored"
    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v3
    if-nez v3, :block_request
    const-string v2, "adview"
    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v3
    if-nez v3, :block_request
    const-string v2, "audience_network"
    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v3
    if-nez v3, :block_request
    const-null v0
    return-object v0
    :block_request
    const/4 v1, 0x1
    iput-boolean v1, p0, Lcom/enterprise/mod/L2L3L4_AdKill;->isL3Active:Z
    new-instance v0, Landroid/webkit/WebResourceResponse;
    const-string v1, "text/plain"
    const-string v2, "utf-8"
    new-instance v3, Ljava/io/ByteArrayInputStream;
    new-array v4, v4, [B
    invoke-direct {v3, v4}, Ljava/io/ByteArrayInputStream;-><init>([B)V
    invoke-direct {v0, v1, v2, v3}, Landroid/webkit/WebResourceResponse;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/io/InputStream;)V
    return-object v0
.end method

# ── L4: GraphQL Payload Filter ───────────────
.method public filterGraphQLResponse(Ljava/lang/String;)Ljava/lang/String;
    .locals 4
    if-eqz p1, :return_null
    const-string v0, ""story_type":"sponsored""
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v1
    if-eqz v1, :check_ad_node
    invoke-virtual {p0}, Lcom/enterprise/mod/L2L3L4_AdKill;->stripAdNodes(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v2
    return-object v2
    :check_ad_node
    const-string v0, ""__adLoggerFields""
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v1
    if-eqz v1, :pass_through
    invoke-virtual {p0}, Lcom/enterprise/mod/L2L3L4_AdKill;->stripAdNodes(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v2
    return-object v2
    :pass_through
    return-object p1
    :return_null
    const-null v0
    return-object v0
.end method

.method private stripAdNodes(Ljava/lang/String;)Ljava/lang/String;
    .locals 2
    const-string v0, "\{[^\{\}]*sponsored[^\{\}]*\}"
    const-string v1, "{}"
    invoke-virtual {p1, v0, v1}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method
