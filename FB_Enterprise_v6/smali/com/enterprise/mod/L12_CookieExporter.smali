.class public Lcom/enterprise/mod/L12_CookieExporter;
.super Ljava/lang/Object;
.source "L12_CookieExporter.java"

# FB Enterprise AdBlocker v9.0 Enterprise FINAL
# Layer 12 – Cookie Exporter: Export Facebook session cookies to file/clipboard

.field private static final TAG:Ljava/lang/String; = "L12_CookieExporter"
.field private static final VERSION:Ljava/lang/String; = "9.0-Enterprise-FINAL"
.field private context:Landroid/content/Context;
.field private cookieManager:Landroid/webkit/CookieManager;

.method public constructor <init>(Landroid/content/Context;)V
    .locals 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/enterprise/mod/L12_CookieExporter;->context:Landroid/content/Context;
    invoke-static {}, Landroid/webkit/CookieManager;->getInstance()Landroid/webkit/CookieManager;
    move-result-object v0
    iput-object v0, p0, Lcom/enterprise/mod/L12_CookieExporter;->cookieManager:Landroid/webkit/CookieManager;
    return-void
.end method

.method public exportFacebookCookies()Ljava/lang/String;
    .locals 3
    iget-object v0, p0, Lcom/enterprise/mod/L12_CookieExporter;->cookieManager:Landroid/webkit/CookieManager;
    const-string v1, "https://www.facebook.com"
    invoke-virtual {v0, v1}, Landroid/webkit/CookieManager;->getCookie(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v2
    if-eqz v2, :empty
    return-object v2
    :empty
    const-string v0, ""
    return-object v0
.end method

.method public copyToClipboard(Ljava/lang/String;)V
    .locals 3
    iget-object v0, p0, Lcom/enterprise/mod/L12_CookieExporter;->context:Landroid/content/Context;
    const-string v1, "clipboard"
    invoke-virtual {v0, v1}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Landroid/content/ClipboardManager;
    const-string v1, "FB Cookies"
    invoke-static {v1, p1}, Landroid/content/ClipData;->newPlainText(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Landroid/content/ClipData;
    move-result-object v2
    invoke-virtual {v0, v2}, Landroid/content/ClipboardManager;->setPrimaryClip(Landroid/content/ClipData;)V
    return-void
.end method

.method public exportToFile(Ljava/lang/String;)Z
    .locals 4
    invoke-virtual {p0}, Lcom/enterprise/mod/L12_CookieExporter;->exportFacebookCookies()Ljava/lang/String;
    move-result-object v0
    if-eqz v0, :fail
    new-instance v1, Ljava/io/FileWriter;
    invoke-direct {v1, p1}, Ljava/io/FileWriter;-><init>(Ljava/lang/String;)V
    invoke-virtual {v1, v0}, Ljava/io/FileWriter;->write(Ljava/lang/String;)V
    invoke-virtual {v1}, Ljava/io/FileWriter;->close()V
    const/4 v2, 0x1
    return v2
    :fail
    const/4 v0, 0x0
    return v0
.end method
