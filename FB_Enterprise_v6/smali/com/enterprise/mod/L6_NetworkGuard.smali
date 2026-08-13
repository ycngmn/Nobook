.class public Lcom/enterprise/mod/L6_NetworkGuard;
.super Ljava/lang/Object;
.source "L6_NetworkGuard.java"

# FB Enterprise AdBlocker v7.0 Enterprise FINAL
# Layer 6 – Network Guard: DNS/Host level blocking + SSL pinning bypass

.field private static final TAG:Ljava/lang/String; = "L6_NetworkGuard"
.field private static final VERSION:Ljava/lang/String; = "7.0-Enterprise-FINAL"
.field private blockedHosts:Ljava/util/HashSet;
.field private bypassSslPinning:Z

.method public constructor <init>()V
    .locals 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    new-instance v0, Ljava/util/HashSet;
    invoke-direct {v0}, Ljava/util/HashSet;-><init>()V
    iput-object v0, p0, Lcom/enterprise/mod/L6_NetworkGuard;->blockedHosts:Ljava/util/HashSet;
    invoke-virtual {p0}, Lcom/enterprise/mod/L6_NetworkGuard;->populateBlockList()V
    const/4 v1, 0x1
    iput-boolean v1, p0, Lcom/enterprise/mod/L6_NetworkGuard;->bypassSslPinning:Z
    return-void
.end method

.method private populateBlockList()V
    .locals 2
    iget-object v0, p0, Lcom/enterprise/mod/L6_NetworkGuard;->blockedHosts:Ljava/util/HashSet;
    const-string v1, "an.facebook.com"
    invoke-virtual {v0, v1}, Ljava/util/HashSet;->add(Ljava/lang/Object;)Z
    const-string v1, "connect.facebook.net"
    invoke-virtual {v0, v1}, Ljava/util/HashSet;->add(Ljava/lang/Object;)Z
    const-string v1, "pixel.facebook.com"
    invoke-virtual {v0, v1}, Ljava/util/HashSet;->add(Ljava/lang/Object;)Z
    const-string v1, "graph.facebook.com"
    invoke-virtual {v0, v1}, Ljava/util/HashSet;->add(Ljava/lang/Object;)Z
    return-void
.end method

.method public isHostBlocked(Ljava/lang/String;)Z
    .locals 2
    iget-object v0, p0, Lcom/enterprise/mod/L6_NetworkGuard;->blockedHosts:Ljava/util/HashSet;
    invoke-virtual {v0, p1}, Ljava/util/HashSet;->contains(Ljava/lang/Object;)Z
    move-result v1
    return v1
.end method
