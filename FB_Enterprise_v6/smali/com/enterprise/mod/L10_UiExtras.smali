.class public Lcom/enterprise/mod/L10_UiExtras;
.super Ljava/lang/Object;
.source "L10_UiExtras.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# Layer 10 – UI Extras: Dark mode force, font scale, hide elements

.field private static final TAG:Ljava/lang/String; = "L10_UiExtras"
.field private static final VERSION:Ljava/lang/String; = "8.0-Enterprise-FINAL"
.field private context:Landroid/content/Context;
.field private darkModeEnabled:Z
.field private fontScaleFactor:F

.method public constructor <init>(Landroid/content/Context;)V
    .locals 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcom/enterprise/mod/L10_UiExtras;->context:Landroid/content/Context;
    const/4 v0, 0x0
    iput-boolean v0, p0, Lcom/enterprise/mod/L10_UiExtras;->darkModeEnabled:Z
    const v0, 0x3F800000   # 1.0f
    iput v0, p0, Lcom/enterprise/mod/L10_UiExtras;->fontScaleFactor:F
    return-void
.end method

.method public applyDarkMode(Landroid/webkit/WebView;)V
    .locals 2
    iget-boolean v0, p0, Lcom/enterprise/mod/L10_UiExtras;->darkModeEnabled:Z
    if-eqz v0, :skip
    const-string v1, "javascript:(function(){document.documentElement.style.filter='invert(1) hue-rotate(180deg)';document.querySelectorAll('img,video').forEach(el=>{el.style.filter='invert(1) hue-rotate(180deg)';});})();"
    invoke-virtual {p1, v1}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V
    :skip
    return-void
.end method

.method public setDarkMode(Z)V
    .locals 0
    iput-boolean p1, p0, Lcom/enterprise/mod/L10_UiExtras;->darkModeEnabled:Z
    return-void
.end method
