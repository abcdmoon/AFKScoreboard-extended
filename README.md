# AFKScoreboard
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/AFKScoreboard.svg)](https://github.com/gorogoro-space/AFKScoreboard/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/AFKScoreboard/issues)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://github.com/gorogoro-space/AFKScoreboard/blob/main/LICENSE)

This plugin works with AxAFKZone to display a ranking scoreboard based on AFK time.

# I haven't tested whether it works, but...
It may work if the conditions of using **Java 21** or earlier and **Paper 26.2** or earlier are met.

# Installation method
Please place the .jar file in the Paper plugins folder.

# Useage
```
/afkhide   放置ランキングから自分を表示/非表示できます
```

# Disclaimer
Do not assume any responsibility by use. Please use it at your own risk.

# 上記から改変したこと
## コマンド
```
/afkhide or /afkscore hide   放置ランキングから自分を表示/非表示できます (権限必要なし)
/afkscore reload ゲーム内で放置エリアを編集などして変わったファイルを再読込する
```
## 仕様
・/afkhideをしても記録自体はとられ、表示すると密かに記録していた値が出るようになりました
・領域が直方体であることを前提にコードを書き換えたためもし直方体以外の領域があると機能しません
