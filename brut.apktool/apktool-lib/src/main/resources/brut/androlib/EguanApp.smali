.class public Lcom/reverse/stub/EguanApp;
.super Landroid/app/Application;
.source "EguanApp.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 18
    invoke-direct {p0}, Landroid/app/Application;-><init>()V

    return-void
.end method

.method private initSDK()V
    .locals 4

    .line 34
    const/4 v0, 0x2

    invoke-static {p0, v0}, Lcom/analysys/AnalysysAgent;->setDebugMode(Landroid/content/Context;I)V

    .line 36
    new-instance v0, Lcom/analysys/AnalysysConfig;

    invoke-direct {v0}, Lcom/analysys/AnalysysConfig;-><init>()V

    .line 38
    .local v0, "config":Lcom/analysys/AnalysysConfig;
    const-string v1, "2709692586aa3e42"

    invoke-virtual {v0, v1}, Lcom/analysys/AnalysysConfig;->setAppKey(Ljava/lang/String;)V

    .line 40
    const-string v1, "AnalsysyReverse"

    invoke-virtual {v0, v1}, Lcom/analysys/AnalysysConfig;->setChannel(Ljava/lang/String;)V

    .line 42
    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Lcom/analysys/AnalysysConfig;->setAutoProfile(Z)V

    .line 44
    sget-object v2, Lcom/analysys/EncryptEnum;->AES:Lcom/analysys/EncryptEnum;

    invoke-virtual {v0, v2}, Lcom/analysys/AnalysysConfig;->setEncryptType(Lcom/analysys/EncryptEnum;)V

    .line 46
    invoke-virtual {v0, v1}, Lcom/analysys/AnalysysConfig;->setAllowTimeCheck(Z)V

    .line 48
    const-wide/16 v2, 0x12c

    invoke-virtual {v0, v2, v3}, Lcom/analysys/AnalysysConfig;->setMaxDiffTimeInterval(J)V

    .line 50
    invoke-virtual {v0, v1}, Lcom/analysys/AnalysysConfig;->setAutoInstallation(Z)V

    .line 52
    const/4 v2, 0x0

    invoke-virtual {v0, v2}, Lcom/analysys/AnalysysConfig;->setAutoHeatMap(Z)V

    .line 54
    invoke-virtual {v0, v1}, Lcom/analysys/AnalysysConfig;->setAutoTrackPageView(Z)V

    .line 56
    invoke-virtual {v0, v2}, Lcom/analysys/AnalysysConfig;->setAutoTrackFragmentPageView(Z)V

    .line 58
    invoke-virtual {v0, v2}, Lcom/analysys/AnalysysConfig;->setAutoTrackClick(Z)V

    .line 60
    invoke-virtual {v0, v1}, Lcom/analysys/AnalysysConfig;->setEnableException(Z)V

    .line 62
    invoke-static {p0, v0}, Lcom/analysys/AnalysysAgent;->init(Landroid/content/Context;Lcom/analysys/AnalysysConfig;)V

    .line 64
    const-string v1, "https://arkpaastest.analysys.cn:4089"

    invoke-static {p0, v1}, Lcom/analysys/AnalysysAgent;->setUploadURL(Landroid/content/Context;Ljava/lang/String;)V

    .line 66
    const-string v2, "wss://arkpaastest.analysys.cn:4091"

    invoke-static {p0, v2}, Lcom/analysys/AnalysysAgent;->setVisitorDebugURL(Landroid/content/Context;Ljava/lang/String;)V

    .line 68
    invoke-static {p0, v1}, Lcom/analysys/AnalysysAgent;->setVisitorConfigURL(Landroid/content/Context;Ljava/lang/String;)V

    .line 69
    return-void
.end method

.method private strictMode()V
    .locals 1

    .line 75
    new-instance v0, Landroid/os/StrictMode$ThreadPolicy$Builder;

    invoke-direct {v0}, Landroid/os/StrictMode$ThreadPolicy$Builder;-><init>()V

    .line 76
    invoke-virtual {v0}, Landroid/os/StrictMode$ThreadPolicy$Builder;->detectAll()Landroid/os/StrictMode$ThreadPolicy$Builder;

    move-result-object v0

    .line 77
    invoke-virtual {v0}, Landroid/os/StrictMode$ThreadPolicy$Builder;->penaltyLog()Landroid/os/StrictMode$ThreadPolicy$Builder;

    move-result-object v0

    .line 78
    invoke-virtual {v0}, Landroid/os/StrictMode$ThreadPolicy$Builder;->build()Landroid/os/StrictMode$ThreadPolicy;

    move-result-object v0

    .line 75
    invoke-static {v0}, Landroid/os/StrictMode;->setThreadPolicy(Landroid/os/StrictMode$ThreadPolicy;)V

    .line 79
    new-instance v0, Landroid/os/StrictMode$VmPolicy$Builder;

    invoke-direct {v0}, Landroid/os/StrictMode$VmPolicy$Builder;-><init>()V

    .line 80
    invoke-virtual {v0}, Landroid/os/StrictMode$VmPolicy$Builder;->detectAll()Landroid/os/StrictMode$VmPolicy$Builder;

    move-result-object v0

    .line 81
    invoke-virtual {v0}, Landroid/os/StrictMode$VmPolicy$Builder;->penaltyLog()Landroid/os/StrictMode$VmPolicy$Builder;

    move-result-object v0

    .line 82
    invoke-virtual {v0}, Landroid/os/StrictMode$VmPolicy$Builder;->build()Landroid/os/StrictMode$VmPolicy;

    move-result-object v0

    .line 79
    invoke-static {v0}, Landroid/os/StrictMode;->setVmPolicy(Landroid/os/StrictMode$VmPolicy;)V

    .line 83
    return-void
.end method


# virtual methods
.method public onCreate()V
    .locals 0

    .line 23
    invoke-super {p0}, Landroid/app/Application;->onCreate()V

    .line 25
    invoke-direct {p0}, Lcom/reverse/stub/EguanApp;->strictMode()V

    .line 27
    invoke-direct {p0}, Lcom/reverse/stub/EguanApp;->initSDK()V

    .line 28
    return-void
.end method
