<p align="center">
  <img src="docs/icon.png" alt="Fancy Health Indicators" width="128">
</p>

<h1 align="center">Fancy Health Indicators</h1>

<p align="center">
  Health bars and floating damage numbers with a settings screen where every change shows up at once.<br>
  A client-side mod for <b>Fabric</b>, <b>Quilt</b>, <b>NeoForge</b> and <b>Forge</b>, <b>Minecraft 1.16.5 – 26.3</b>. 18 languages.
</p>

<p align="center">
  <b>English</b> | <a href="README.ru.md">Русский</a>
</p>

![Bars and damage numbers](docs/img/fight.gif)

## What it does

Fancy Health Indicators shows how much health every mob and player has, right where you are looking, and how much damage
each of your hits deals. Vanilla Minecraft never tells you an enemy's health: this mod puts a health bar (or hearts) above
every creature and makes the damage of every hit fly out as a number.

- **You always know who is about to die.** In a fight you see at a glance which zombie is almost dead and which one is
  still at full health, so you pick targets, finish weak mobs first and know when to retreat.
- **You see the real damage of your weapons.** Floating numbers show every hit, crit, fire, poison, wither or fall damage,
  so you can compare swords, axes, enchantments and potions by numbers instead of guessing.
- **Big mobs and bosses become readable.** An Iron Golem, a Warden or the Wither have far more than 10 hearts; the mod shows
  them as rows of hearts, colored layers or one bar with a number, so you can follow a long fight.
- **PvP: the opponent's health above their name,** like the vanilla scoreboard under the nickname, but without any plugin
  on the server.
- **It looks the way you like.** 11 ready presets change the whole look in one click, and the settings screen has a live
  preview: every slider you move is shown on a mob right away.

Works on any server: everything is drawn on your side, the server doesn't need anything installed. The mod doesn't change
game mechanics, it only shows information the game already has.

## Features

- **Health bars** above the head, above or below the nametag, on the body, under the crosshair, or as a target frame
  at the top of the screen.
- **The name above the bar** has its own size, font, color and effect.
- **Twelve bar styles**: flat, glossy, rounded, segmented, slanted, neon, MMO plate, thin line, hearts, hearts from the game HUD,
  scoreboard line under the nick (`20 ❤`, like the vanilla belowname) and text only.
- **Big mobs**: more than 10 hearts are shown in rows like the game HUD, as colored layers of 20 HP, or as one heart
  with a number. Poison, wither and freezing change the hearts like in the HUD.
- **HP drain**: the lost part of the bar holds for a moment and then drains away; the delay, speed and colors are yours.
  A hit flashes only the part that was lost.
- **Damage numbers**: pop, rise, fountain, bounce, zoom slam, shake, slide, spiral and a stacked log. Crits get their own
  animation, size, color and mark. Colors by damage type (fire, magic, fall…), quick hits merge into one number.
- **12 fonts** for bars and numbers, with shadow, outline, thick outline or glow.
- **11 presets**: Classic, MMO, Hearts, Game hearts, Scoreboard, Hologram, Arcade, Minimal, PvP, Boss bar, Comic.
  Each shows a live sample.
- **Live preview**: the mob on the right reacts to every change — pick an animation and a number flies out,
  move a slider and the bar moves. LMB hits, RMB crits, MMB heals.
- **Search** over all settings: by name, by value, by category, by color (`red`, `#ff0000`), by typos or a wrong
  keyboard layout, and `changed` for everything you changed.
- **Fair play**: no bars on invisible players and nothing through walls unless the nametag itself is visible there.
- **Server nametags**: bars move above custom nametags made of text displays and above the TAB line under the nick;
  player HP can be read from that line when the server hides the real one.
- **Zoom mods** (Zoomify, Ok Zoomer, Logical Zoom): bars and numbers keep the right size while zoomed.

## Presets

| | | |
|---|---|---|
| ![Classic](docs/img/hero.jpg) Classic | ![MMO](docs/img/preset-mmo.jpg) MMO | ![Hologram](docs/img/preset-holo.jpg) Hologram |
| ![Hearts](docs/img/preset-hearts.jpg) Hearts | ![Arcade](docs/img/preset-arcade.jpg) Arcade | ![PvP](docs/img/preset-pvp.jpg) PvP |
| ![Comic](docs/img/preset-comic.jpg) Comic | ![Minimal](docs/img/preset-minimal.jpg) Minimal | ![Boss bar](docs/img/preset-boss.jpg) Boss bar |
| ![Game hearts](docs/img/preset-game_hearts.jpg) Game hearts | ![Scoreboard](docs/img/preset-scoreboard.jpg) Scoreboard | |

A preset is a starting point: change anything after it and the preset becomes "Custom".

## Settings

Open with `H` (Controls → Fancy Health Indicators) or through Mod Menu. The switch at the top turns the whole mod off,
and a separate key for that can be bound in the controls.

![Settings](docs/img/menu-overhead.jpg)

![Live preview](docs/img/preview.gif)

| | |
|---|---|
| ![Presets](docs/img/menu-presets.jpg) Presets | ![Damage numbers](docs/img/menu-numbers.jpg) Damage numbers |
| ![Filters](docs/img/menu-filters.jpg) Filters: click a mob on the right to turn it on or off | ![Top HUD](docs/img/menu-target.jpg) Target frame |
| ![Search](docs/img/menu-search.jpg) Search | ![Search by color](docs/img/menu-search-red.jpg) Search by color |

- RMB on a slider — type a number, RMB on a color — type a hex code.
- `↺` next to a setting puts it back to the preset value.
- "Reset tab" resets the current tab, Shift + click resets everything; what will be reset is highlighted in green.
- Export and Import on the Presets tab copy the whole config as a short string through the clipboard.
- Settings that only work together with another one are dimmed, and changing them switches the other one on.

| | |
|---|---|
| ![Crosshair and target frame](docs/img/crosshair-target.jpg) Bar under the crosshair and the target frame | ![Nametags](docs/img/nametags.jpg) Bars above a server nametag |

## For server owners

The mod can be limited per server without any plugin: send one of these codes in a chat message, or put it into the server
MOTD. The codes are invisible, and a message made only of codes doesn't show up in chat at all.

| Code | Turns off |
|---|---|
| `§h§p§j` | the whole mod |
| `§h§p§g` | bars (above head, under the crosshair, target frame) |
| `§h§p§q` | damage numbers |
| `§h§p§s` | bars and numbers on other players |
| `§h§p§w` | bars behind walls, even when the nametag is visible there |
| `§h§p§z` | everything above, back on |

Codes can be combined in one message. They last until the player leaves the server, and the settings screen shows the
player what the server turned off.

Bukkit / Paper:

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    event.getPlayer().sendMessage("§h§p§s§h§p§q");
}
```

Other client mods can do the same through the API:

```java
FancyHealthApi.restrict("mymod", Restriction.PLAYERS, Restriction.NUMBERS);
FancyHealthApi.clear("mymod");
```

## Updates

When a new version is out on Modrinth, a small card shows up in the corner of the settings screen with Download, Hide and
Don't remind. The check is one request to the Modrinth API when the settings are opened for the first time; it can be
turned off in General.

## Install

Put the jar for your Minecraft version and loader into `mods`. On Fabric and Quilt you also need
[Fabric API](https://modrinth.com/mod/fabric-api) (QFAPI on Quilt); [Mod Menu](https://modrinth.com/mod/modmenu) is optional.
On NeoForge and Forge the settings open from the mod list.

| Minecraft | Fabric / Quilt | NeoForge | Forge |
|---|:-:|:-:|:-:|
| 26.3 | ✔ | ✔ | |
| 26.2 | ✔ | | |
| 26.1 – 26.1.2 | ✔ | | |
| 1.21.11 | ✔ | ✔ | |
| 1.21.9 – 1.21.10 | ✔ | | |
| 1.21.6 – 1.21.8 | ✔ | | |
| 1.21.5 | ✔ | | |
| 1.21.4 | ✔ | | |
| 1.21.2 – 1.21.3 | ✔ | | |
| 1.21 – 1.21.1 | ✔ | ✔ | |
| 1.20.5 – 1.20.6 | ✔ | | |
| 1.20.3 – 1.20.4 | ✔ | | |
| 1.20 – 1.20.1 | ✔ | | ✔ |
| 1.19.4 | ✔ | | |
| 1.19.1 – 1.19.2 | ✔ | | |
| 1.18.2 | ✔ | | |
| 1.17.1 | ✔ | | |
| 1.16.5 | ✔ | | |

Java: 8+ for 1.16.5, 16+ for 1.17.1, 17+ up to 1.20.4, 21+ for 1.20.5 – 1.21.11, 25+ for 26.x — the same Java the game itself needs.

The config is `config/fancyhealth.json`.

## Languages

en, ru, uk, de, fr, es, pt, it, pl, tr, nl, cs, sv, zh, ja, ko, vi, id. The mod follows the game language.

## Build

JDK 25. All code lives in `common/`; lines that differ between versions and loaders are marked with `//? if` comments and
switched by a small preprocessor in `common/preprocess.gradle`. Every target in `versions/` is a separate Gradle build:

```bash
cd versions/1.21.11 && ./gradlew build
cd versions/neoforge-1.21.1 && ./gradlew build
cd versions/forge-1.20.1 && ./gradlew build
```

Jars end up in `versions/<target>/build/libs`. `./gradlew activate` in any target switches the sources in `common/` to that
version, so the IDE sees the right branches.

## License

[MIT](LICENSE). Use it however and wherever you like; the only ask is to keep a link to this repository and the author's
nickname, **BbIJABNPOBATEJb**.
