.class public Lcom/enterprise/mod/ModInit;
.super Ljava/lang/Object;
.source "ModInit.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# ModInit – Central bootstrap, hooks all layers into FB runtime

.field private static volatile INSTANCE:Lcom/enterprise/mod/ModInit;
.field private static final TAG:Ljava/lang/String; = "ModInit_v8.0"
.field private l1Engine:Lcom/enterprise/mod/L1_AdBlockEngine;
.field private l2l3l4:Lcom/enterprise/mod/L2L3L4_AdKill;
.field private l5:Lcom/enterprise/mod/L5_UrlGuard;
.field private l6:Lcom/enterprise/mod/L6_NetworkGuard;
.field private l7:Lcom/enterprise/mod/L7_Watchdog;
.field private l8:Lcom/enterprise/mod/L8_VideoDownloader;
.field private l9:Lcom/enterprise/mod/L9_FeedFilter;
.field private l10:Lcom/enterprise/mod/L10_UiExtras;
.field private l11:Lcom/enterprise/mod/L11_UpdateBlocker;
.field private l12:Lcom/enterprise/mod/L12_CookieExporter;
.field private l13:Lcom/enterprise/mod/L13_VirtualCamera;
.field private initialized:Z
.field private context:Landroid/content/Context;

.method public static getInstance()Lcom/enterprise/mod/ModInit;
    .locals 2
    sget-object v0, Lcom/enterprise/mod/ModInit;->INSTANCE:Lcom/enterprise/mod/ModInit;
    if-nez v0, :return
    new-instance v0, Lcom/enterprise/mod/ModInit;
    invoke-direct {v0}, Lcom/enterprise/mod/ModInit;-><init>()V
    sput-object v0, Lcom/enterprise/mod/ModInit;->INSTANCE:Lcom/enterprise/mod/ModInit;
    :return
    sget-object v0, Lcom/enterprise/mod/ModInit;->INSTANCE:Lcom/enterprise/mod/ModInit;
    return-object v0
.end method

.method public init(Landroid/content/Context;)V
    .locals 2
    iget-boolean v0, p0, Lcom/enterprise/mod/ModInit;->initialized:Z
    if-nez v0, :skip
    iput-object p1, p0, Lcom/enterprise/mod/ModInit;->context:Landroid/content/Context;
    # Init L1
    invoke-static {p1}, Lcom/enterprise/mod/L1_AdBlockEngine;->getInstance(Landroid/content/Context;)Lcom/enterprise/mod/L1_AdBlockEngine;
    move-result-object v0
    iput-object v0, p0, Lcom/enterprise/mod/ModInit;->l1Engine:Lcom/enterprise/mod/L1_AdBlockEngine;
    # Init L2L3L4
    new-instance v0, Lcom/enterprise/mod/L2L3L4_AdKill;
    invoke-direct {v0}, Lcom/enterprise/mod/L2L3L4_AdKill;-><init>()V
    iput-object v0, p0, Lcom/enterprise/mod/ModInit;->l2l3l4:Lcom/enterprise/mod/L2L3L4_AdKill;
    # Init L7 Watchdog
    new-instance v0, Lcom/enterprise/mod/L7_Watchdog;
    invoke-direct {v0}, Lcom/enterprise/mod/L7_Watchdog;-><init>()V
    iput-object v0, p0, Lcom/enterprise/mod/ModInit;->l7:Lcom/enterprise/mod/L7_Watchdog;
    invoke-virtual {v0}, Lcom/enterprise/mod/L7_Watchdog;->start()V
    # Init L12 CookieExporter
    new-instance v0, Lcom/enterprise/mod/L12_CookieExporter;
    invoke-direct {v0, p1}, Lcom/enterprise/mod/L12_CookieExporter;-><init>(Landroid/content/Context;)V
    iput-object v0, p0, Lcom/enterprise/mod/ModInit;->l12:Lcom/enterprise/mod/L12_CookieExporter;
    const/4 v0, 0x1
    iput-boolean v0, p0, Lcom/enterprise/mod/ModInit;->initialized:Z
    const-string v1, "ModInit"
    const-string v0, "FB Enterprise AdBlocker v8.0 – All layers initialized OK"
    invoke-static {v1, v0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    :skip
    return-void
.end method

.method public areAllLayersActive()Z
    .locals 1
    iget-boolean v0, p0, Lcom/enterprise/mod/ModInit;->initialized:Z
    return v0
.end method

.method public reinjectAllLayers()V
    .locals 1
    iget-object v0, p0, Lcom/enterprise/mod/ModInit;->context:Landroid/content/Context;
    invoke-virtual {p0, v0}, Lcom/enterprise/mod/ModInit;->init(Landroid/content/Context;)V
    return-void
.end method
