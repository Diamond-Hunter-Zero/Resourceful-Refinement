---
title: Choral Clusters
category: Worldgen
status: Implemented
introduced: v0.4
recipe_type: n/a
related:
  - "[[Choral Clusters Worldgen]]"
  - "[[Chorus Crystal Pillars]]"
  - "[[Chorus Crystal]]"
  - "[[Choral End Stone]]"
  - "[[Crystal Fissure Bud]]"
  - "[[Primary Design Doc]]"
tags:
  - worldgen
  - biome
  - overview
---

The **Choral Clusters** are a modded biome that generates in the *outer* End dimension: a mountainous End-stone landscape studded with vast glowing [[Chorus Crystal]] spikes and pillars. It is the End-tier home of the v0.4 crystal extraction loop — the biome where Chorus [[Crystal Fissure Bud|Crystal Fissures]] are intended to spawn for the [[Drill Pylon]] to tap.

This page is the **content-facing overview and hub**. The technical breakdown is split across:

- [[Choral Clusters Worldgen]] — how the biome is placed in the End and how the mountain terrain is built.
- [[Chorus Crystal Pillars]] — the crystal-spike structures that decorate the mountains, and their tuning presets.
- [[Chorus Crystal]] — the glowing translucent crystal block and its world-position colour system.
- [[Choral End Stone]] — the terrain block the mountains are carved from.
- [[Choral Vegetation]] — the `chorus_crystal_growth` and `choral_sprouts` scatter features and their tuning.

**Biome ID:** `resourceful_refinement:choral_clusters`

## Gameplay Role

The Choral Clusters are the End-dimension destination of the v0.4 extraction tiers. Where the surface tier uses [[Mineral Deposit|Mineral Deposits]] and the fluid tier uses the [[Geyser Block]], the crystal tier is sourced from Chorus Crystal Fissures that are *designed* to generate here, rarely, inside the biome. A player who travels far enough out into the End finds mountainous islands blanketed in luminous crystal, and prospects them (eventually with the [[Resource Resonator]]) for fissure sites to build a [[Drill Pylon]] over.

Per the [[Primary Design Doc]], the biome is also the intended natural home of End-tier [[Resonance Crystal|Resonance Crystals]] and "Chorus Crystal Hearts", and the Chorus Crystal itself is the raw material behind Liquid Chorus (which fuels the [[Remote Entanglement]] transporter).

## What Generates Here (current state)

| Element | Status | Notes |
|---|---|---|
| Choral Clusters biome | **Implemented** | Runtime-injected into the outer End, replacing End Highlands/Midlands past 1024 blocks. See [[Choral Clusters Worldgen]]. |
| Mountain terrain | **Implemented** | Raised radial mounds built by the `choral_cluster_mountain` structure — vanilla End stone body, [[Choral End Stone]] skinned onto the exposed surface in noise patches. |
| [[Chorus Crystal Pillars\|Chorus Crystal spikes/pillars]] | **Implemented** | Scattered across mountain surfaces per the active pillar preset. |
| Vanilla chorus plants | **Implemented** | Sparse `minecraft:chorus_plant` via the `sparse_chorus_plant` placed feature (biome `VEGETAL_DECORATION` step). Populate the End-stone surface; skip [[Choral End Stone]] patches (vanilla requires `end_stone` directly below). |
| `chorus_crystal_growth` | **Implemented** | Amethyst-cluster-style decorative crystal, placed by the custom `attached_growth` feature in two passes: **up-facing on top of** end-stone/[[Choral End Stone]] terrain (grass-like), and **any orientation on** [[Chorus Crystal]] faces (up/down/sideways). |
| `choral_sprouts` | **Implemented** | Grass-like block scattered on end-stone / [[Choral End Stone]] terrain via a vanilla `simple_block` feature. |
| Endermen | **Implemented** | Only mob spawn in the biome (weight 50, 1–3). |
| Chorus Crystal Fissures | **Not implemented** | The intended extraction node; no fissure placement exists in the generator yet. |
| Resonance Crystals / Crystal Hearts | **Not implemented** | Design intent only. |
| Other vegetation / ore / decoration features | **None** | Only the chorus-plant vegetal feature is populated; every other generation step is empty. |

> [!note] Implementation
> **Two decoration layers.** The *structural* terrain and crystal spikes are built in Java inside the **custom structure** (`choral_cluster_mountain`) at `raw_generation` — see [[Choral Clusters Worldgen]] and [[Chorus Crystal Pillars]]. The *scatter* decoration (chorus plants, `chorus_crystal_growth`, `choral_sprouts`) runs afterward as ordinary **biome placed-features** in the `VEGETAL_DECORATION` step, so terrain and crystal already exist for them to sit on. Surface scatter uses vanilla feature types; attaching `chorus_crystal_growth` to arbitrary crystal/terrain faces (with correct orientation) uses a small custom feature. Densities and targets are datapack-tweakable — see [[Choral Vegetation]].

## Integration With Other Features

- **[[Crystal Fissure Bud]] / [[Drill Pylon]]** — The Crystal Fissure Bud design names the Choral Clusters as the host for the End variant ("Chorus Crystal Fissures will spawn rarely in the End Islands inside a generation feature called the Choral Clusters"). The hookup is **not built yet**: the mountain piece places terrain and crystal pillars only, never a `crystal_fissure_bud`. Wiring fissures into the generator (1–2 buds per mountain) is the key open integration task.
- **[[Resource Resonator]]** — The planned prospecting tool scans for resource nodes including crystal fissures; once fissures generate here it would surface their coordinates on its radar.
- **[[Resonance Crystal]]** — Intended to generate "commonly in the end"; the Choral Clusters are the natural candidate biome, but no placement exists.
- **[[Remote Entanglement]]** — The transporter is fuelled by Liquid Chorus, the fluid-form of Chorus Crystal; Choral Clusters are the crystal's world source.
- **[[Primary Design Doc]]** — Canonical design description of the biome and its intended contents.

## Open Tuning / Polish Surface

For the upcoming polish pass, the live knobs are:

- `ChoralClusterGenerationSettings` — biome spread (distance + noise threshold), mountain island radius/height, terrain noise, and which [[Chorus Crystal Pillars|pillar preset]] is active (`ACTIVE_PRESET`, currently `MODEST`).
- `ChorusCrystalColorSettings` — the [[Chorus Crystal]] colour bands per dimension.
- `choral_cluster_mountain_set.json` — structure spread (currently a very dense `spacing 3 / separation 2`).
- The biome JSON (`worldgen/biome/choral_clusters.json`) — sky/fog colours, particles, mob spawns.

## Related

- [[Choral Clusters Worldgen]] · [[Chorus Crystal Pillars]] · [[Chorus Crystal]] · [[Choral End Stone]] · [[Choral Vegetation]]
- [[Crystal Fissure Bud]] · [[Drill Pylon]] · [[Resource Resonator]] · [[Resonance Crystal]]
- [[Primary Design Doc]] · [[Codebase Overview]]
