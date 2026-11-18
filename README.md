# SkyBlock Money Advisor

A client-side Fabric mod for Hypixel SkyBlock. It reads **your own** profile from the Hypixel API,
checks your skills, Catacombs level, slayers, coins and the gear you own, then tells you the best
coin-making method you can do right now, plus the next unlock worth chasing.

It is read-only: it never automates gameplay, never touches other players, and only calls the public
Hypixel API with your own key.

## Commands

| Command | What it does |
|---|---|
| `/moneyadvisor key <apikey>` | Saves your Hypixel API key (client-side command, never sent to the server) |
| `/moneyadvisor analyze [profile]` | Top 3 methods for your gear, plus the next unlock |
| `/moneyadvisor list [profile]` | Every method you qualify for, ranked |

## Setup

1. Create an API key at <https://developer.hypixel.net> and run `/moneyadvisor key <your key>`.
2. In SkyBlock: **Menu > Settings > Personal > API Settings**, enable **Inventory** (otherwise gear cannot be read).
3. Run `/moneyadvisor analyze`.

## How the estimate works

`coins/hour = base rate x best gear multiplier you own`

* Base rate: items produced per hour x live Bazaar sell price (falls back to a fixed baseline when Bazaar is offline or a method has no items).
* Gear multiplier: the highest tier in `gearTiers` that matches an item in your armor, equipment, inventory, ender chest or wardrobe.
* Methods you do not qualify for are hidden, except the best "almost unlocked" one.

**These numbers are rough starting values, not measurements.** Edit them: copy `src/main/resources/methods.json`
to `config/skyblock-money-advisor-methods.json` in your Minecraft folder and the mod will use your copy instead.
Item ids ending in `*` match by prefix (for example `FERMENTO_*`).

## Build

Targets **Minecraft 26.2** with **Fabric Loader 0.20.1**. Requires **JDK 25** and Gradle 9.5+.

```
gradle wrapper --gradle-version 9.5.1   # once, to generate ./gradlew
./gradlew build                          # jar in build/libs/
```

No local setup? Push the project to a GitHub repository: the workflow in `.github/workflows/build.yml`
builds it and attaches the `.jar` to the run (Actions tab > latest run > Artifacts). Use the
`skyblock-money-advisor-1.0.0.jar` inside, not the `-sources` jar.

Minecraft 26.x is unobfuscated, so the project uses Mojang names (`Minecraft`, `Component`,
`ChatFormatting`, `ClientCommands`) and the `net.fabricmc.fabric-loom` plugin; there is no Yarn.
Cross-check the versions in `gradle.properties` at <https://fabricmc.net/develop> (a newer Fabric API
build for 26.2 may exist). To target another Minecraft version, change `minecraft_version`,
`fabric_api_version` and the `~26.2` range in `fabric.mod.json`.

## Publish on Modrinth

1. Create the project on modrinth.com (type: Mod, loader: Fabric, environment: client-side).
2. Put its slug/ID in `build.gradle` (`projectId`) and your token in `MODRINTH_TOKEN`.
3. `MODRINTH_TOKEN=xxx ./gradlew modrinth`, or upload `build/libs/skyblock-money-advisor-1.0.0.jar` by hand.

The jar's `fabric.mod.json` already carries the id, version, loader and dependency info Modrinth reads.
Modrinth's rules apply to the project page (original description, correct license, no misleading claims).

## License

MIT (see LICENSE).
