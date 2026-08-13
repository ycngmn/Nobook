.class public Lcom/enterprise/mod/ModInit$WebViewClientHook;
.super Landroid/webkit/WebViewClient;
.source "ModInit.java"
.enclosing class Lcom/enterprise/mod/ModInit;

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# ModInit inner class – WebViewClient hook (shouldOverrideUrlLoading, shouldInterceptRequest, onPageFinished)

.method public shouldOverrideUrlLoading(Landroid/webkit/WebView;Ljava/lang/String;)Z
    .locals 2
    invoke-static {}, Lcom/enterprise/mod/ModInit;->getInstance()Lcom/enterprise/mod/ModInit;
    move-result-object v0
    iget-object v0, v0, Lcom/enterprise/mod/ModInit;->l1Engine:Lcom/enterprise/mod/L1_AdBlockEngine;
    invoke-virtual {v0, p2}, Lcom/enterprise/mod/L1_AdBlockEngine;->shouldBlockUrl(Ljava/lang/String;)Z
    move-result v1
    return v1
.end method

.method public shouldInterceptRequest(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;
    .locals 2
    invoke-static {}, Lcom/enterprise/mod/ModInit;->getInstance()Lcom/enterprise/mod/ModInit;
    move-result-object v0
    iget-object v0, v0, Lcom/enterprise/mod/ModInit;->l2l3l4:Lcom/enterprise/mod/L2L3L4_AdKill;
    invoke-virtual {v0, p1, p2}, Lcom/enterprise/mod/L2L3L4_AdKill;->shouldInterceptRequest(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;
    move-result-object v1
    return-object v1
.end method

.method public onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V
    .locals 2
    invoke-super {p0, p1, p2}, Landroid/webkit/WebViewClient;->onPageFinished(Landroid/webkit/WebView;Ljava/lang/String;)V
    invoke-static {}, Lcom/enterprise/mod/ModInit;->getInstance()Lcom/enterprise/mod/ModInit;
    move-result-object v0
    iget-object v0, v0, Lcom/enterprise/mod/ModInit;->l9:Lcom/enterprise/mod/L9_FeedFilter;
    invoke-virtual {v0}, Lcom/enterprise/mod/L9_FeedFilter;->getFilterScript()Ljava/lang/String;
    move-result-object v1
    invoke-virtual {p1, v1}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V
    return-void
.end method
