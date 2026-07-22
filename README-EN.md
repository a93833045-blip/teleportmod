🌐 **Languages / Языки:** English | [Русский](README.md)

# TeleportMod v1.4.0 (Minecraft 1.21.X)

Fabric client-side mod for fast, keybound teleportation and warp management, featuring `/tpa` support.

> ⚠️ **Requirement:** **[Fabric API](https://modrinth.com)**.

---

## 🕹 Features (Version 1.4.0)

* **Double-Tap:** Critical commands (O, R, C) require a double-tap (400ms).
* **Safety:** Macros auto-stop when GUI is open.
* **Actionbar:** Real-time status notifications.

### Hotkeys

| Key | Command | Function |
| :--- | :--- | :--- |
| **O** / **Щ** | `/home o` | Quick home (rename your home to "o"). |
| **R** / **К** | `/rtp` | Random teleport (20s cooldown). |
| **C** / **С** | `/spawn` | Fast spawn. |
| **P** / **З** | `/warp` | Warp menu (hold) / Quick teleport (tap). |
| **B** / **И** | `/tpa` | **[NEW]** Intelligent TPA engine. |

---

## 🗺 Warp System (P)
* **Hold:** Select warps with arrow keys.
* **Double-Tap:** Instant TP to the first warp.

---

## 👥 TPA Engine & Tracker (B)
* **Tap:** Player status (3s).
* **Hold (>300ms):** Player list (select with arrows).
* **Double-Tap:** Send `/tpa` / `/tp`.
* **Auto-Scanner:** Strips prefixes (e.g., `[Admin]`).

---

## ⚙ Config (M)
* Hold **M** for 3 seconds to open.

---

## 🛠 Roadmap
* [ ] `/m` (Private message shortcut).
