.class public Lcom/enterprise/mod/ModInit$CookieJsInterface;
.super Ljava/lang/Object;
.source "ModInit.java"
.enclosing class Lcom/enterprise/mod/ModInit;

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# ModInit inner class – JS Interface for cookie export from WebView context

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/enterprise/mod/ModInit$CookieJsInterface;
    }
.end annotation

.method public constructor <init>(Lcom/enterprise/mod/L12_CookieExporter;)V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.annotation system Ldalvik/annotation/Throws;
    value = {
        Ljava/lang/Exception;
    }
.end annotation

.method public exportCookies()V
    .locals 2
    invoke-static {}, Lcom/enterprise/mod/ModInit;->getInstance()Lcom/enterprise/mod/ModInit;
    move-result-object v0
    iget-object v1, v0, Lcom/enterprise/mod/ModInit;->l12:Lcom/enterprise/mod/L12_CookieExporter;
    invoke-virtual {v1}, Lcom/enterprise/mod/L12_CookieExporter;->exportFacebookCookies()Ljava/lang/String;
    move-result-object v0
    invoke-virtual {v1, v0}, Lcom/enterprise/mod/L12_CookieExporter;->copyToClipboard(Ljava/lang/String;)V
    return-void
.end method
