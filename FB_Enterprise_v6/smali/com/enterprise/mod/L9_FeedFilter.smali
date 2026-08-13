.class public Lcom/enterprise/mod/L9_FeedFilter;
.super Ljava/lang/Object;
.source "L9_FeedFilter.java"

# FB Enterprise AdBlocker v8.0 Enterprise FINAL
# Layer 9 – Feed Filter: Remove suggested posts, reels ads, stories ads

.field private static final TAG:Ljava/lang/String; = "L9_FeedFilter"
.field private static final VERSION:Ljava/lang/String; = "8.0-Enterprise-FINAL"
.field private filterSuggested:Z
.field private filterReelsAds:Z
.field private filterStoriesAds:Z
.field private filterGroupSuggestions:Z

.method public constructor <init>()V
    .locals 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    const/4 v0, 0x1
    iput-boolean v0, p0, Lcom/enterprise/mod/L9_FeedFilter;->filterSuggested:Z
    iput-boolean v0, p0, Lcom/enterprise/mod/L9_FeedFilter;->filterReelsAds:Z
    iput-boolean v0, p0, Lcom/enterprise/mod/L9_FeedFilter;->filterStoriesAds:Z
    iput-boolean v0, p0, Lcom/enterprise/mod/L9_FeedFilter;->filterGroupSuggestions:Z
    return-void
.end method

.method public getFilterScript()Ljava/lang/String;
    .locals 1
    # Returns JS to run in WebView to strip feed ad units
    const-string v0, "javascript:(function(){var selectors=['[data-pagelet=\"FeedUnit\"]','[data-ad-comet-preview-id]','[role=\"feed\"] [aria-label=\"Suggested for you\"]','[aria-label=\"Sponsored\"]','[data-sigil=\"m-feed-voice-subtitle\"]'];selectors.forEach(s=>{document.querySelectorAll(s).forEach(e=>{var l=e.innerText||'';if(l.includes('Sponsored')||l.includes('Suggested'))e.closest('[data-pagelet]')?.remove();});});})();"
    return-object v0
.end method

.method public shouldFilterStoryItem(Ljava/lang/String;)Z
    .locals 2
    if-eqz p1, :no
    const-string v0, "is_sponsored"
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
    move-result v1
    if-nez v1, :yes
    const-string v0, "suggested_unit"
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
