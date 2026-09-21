# AFKScoreboard
This project is a modified version of gorogoro-space/AFKScoreboard.

This project is based on the original AFKScoreboard by gorogoro-space.

Original repository: https://github.com/gorogoro-space/AFKScoreboard
Original author: gorogoro-space
License: GNU General Public License v3.0 (GPL-3.0)
Modification: This repository contains modifications to the original project.
Modified: 2026

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
権限必要なし
/afkhide or /afkscore hide   放置ランキングから自分を表示/非表示できます
/afkscore highscore   プラグインが起動してから今までの自身の最高連続放置時間が確認できる
/afkscore highscore rank   プラグインが起動してから今までの最高連続放置時間ランキング(オフラインプレイヤーも対象)が確認できる
/afkscore role hide   肩書の表示を隠す
/afkscore role <表示名>   表示できる肩書から表示する肩書を選ぶ
要OP
/afkscore reload ゲーム内で放置エリアを編集などして変わったファイルを再読込する
/afkscore highscore <Player>   任意のプレイヤーのハイスコアが確認できる
/afkscore reloadrole   prefix.ymlの内容を読み込み直します
```
## 仕様
・/afkhideをしても記録自体はとられ、表示すると密かに記録していた値が出るようになりました  
・領域が直方体であることを前提にコードを書き換えたためもし直方体以外の領域があると機能しません  
・プレイヤー毎の最高連続放置時間が確認できるようになりました  
　(サーバーの停止処理とともにこのデータは消えます)  
・放置のハイスコアに応じて頭の上に称号がつけられます  
・BukkitのAPIに非同期で触れるのは非推奨とのことで、変更時プラグイン上のFileConfigurationインスタンスに反映  
　一時間毎及びプラグイン停止時にファイルに書き込む仕様としました

## 注意事項
・称号の表示に、スコアボードとチームを用いているので、他プラグインとの衝突が起こる可能性があります
