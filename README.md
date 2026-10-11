# Kecskeméti menetrend alkalmazás

Kecskemét helyi buszjáratainak menetrendjét és a járművek valós idejű helyzetét megjelenítő Android alkalmazás.

## Funkciók

- Vonalak listája és a vonalak indulási menetrendje órákra bontva
- Egy adott járat útvonala a megállókkal, érkezési időkkel és valós idejű késéssel
- Megállók listája kereséssel és rendezéssel
- Megálló kocsiállásainak megjelenítése térképen
- Megálló menetrendje: a következő érkező járatok és a hátralévő idő

## Felületek

Az alkalmazás két fő fülből áll, amelyek között az alsó navigációs sávval lehet váltani.

### Vonalak

| Felület | Leírás |
| --- | --- |
| **Vonalak** | Az összes vonal listája (vonalszám és vonalnév). Az eszköztáron kiválasztható a nap, amelyre a menetrendet lekérjük. |
| **Vonal menetrend** | A kiválasztott vonal indulásai órák szerint csoportosítva, az aktuális órától kezdve. Nem körjárat esetén az irány megfordítható. |
| **Járat útvonal** | Egy konkrét járat megállói sorrendben, érkezési időkkel. A jármű valós idejű pozíciója alapján jelölve vannak az elhagyott megállók, a következőknél pedig a késés/sietés percben. Egy megállóra koppintva megnyílik annak menetrendje. |

### Megállók

| Felület | Leírás |
| --- | --- |
| **Megállók / Kedvencek** | Váltható lista az összes megállóról vagy a kedvencekről. Keresés név szerint, rendezés A → Z vagy Z → A. A kedvencek átnevezhetők és törölhetők. |
| **Megálló kiválasztó** | A megálló kocsiállásai térképen. Egy kocsiállásra koppintva megnyílik annak menetrendje. |
| **Megálló menetrend** | A megállóba érkező következő járatok: vonalszám, érkezési idő, hátralévő percek. Itt lehet a megállót kedvencnek jelölni. Egy járatra koppintva megnyílik annak útvonala. |

## API

Az adatok Kecskemét nyilvános menetrend API-jából származnak:

```
https://menetrend.kecskemet.hu/api/
```

Minden végpont JSON tömböt ad vissza. A `{datum}` paraméter a lekérdezett nap `yyyyMMdd` formátumban (pl. `20261011`).

| Végpont | Leírás | Fontosabb mezők |
| --- | --- | --- |
| `tracks/{datum}` | Az adott napon közlekedő vonalak | `vonal_id`, `vonal_nev`, `korjarat` |
| `routes/{datum}/{vonal_id}/{irany}` | Egy vonal indulásai irányonként (`1` vagy `2`), órákra bontva | `ora`, `jaratok[]` (`jarat_id`, `perc`, `wifi`, `alacsonypadlo`, `online`, `kimaradt`) |
| `path/{datum}/{jarat_id}` | Egy járat útvonala | `sorrend`, `megallo_id`, `megallo_nev`, `erkezesi_ido` |
| `vehicleposition/{datum}/{jarat_id}` | A járművek valós idejű helyzete | `utolso_megallo_id`, `utolso_megallo_sorszam`, `megalloban`, `keses_sietes` (másodperc) |
| `geolocs/{datum}` | Az összes megálló | `foldhely_id`, `foldhely_nev` |
| `platforms/{datum}/{foldhely_id}` | Egy megálló kocsiállásai GPS koordinátákkal | `kocsiall_id`, `kocsiall_nev`, `kocsiall_szam`, `kocsiall_gps_x`, `kocsiall_gps_y` |
| `stopschedule/{datum}/04:00/{kocsiall_id}` | Egy kocsiállásba érkező járatok | `jarat_id`, `vonal_id`, `nyomvonal_nev`, `erkezes`, `erk_minute`, `megalloban` |

Megjegyzések:

- `korjarat`: `"1"` esetén körjárat (egy irány), `"0"` esetén a vonalnak két iránya van.
- `megalloban`: `"1"`, ha a jármű éppen a megállóban áll.
- `keses_sietes`: pozitív érték késést, negatív sietést jelent, másodpercben.

## Technológiák

- **Nyelv:** Java, minimum Android 7.0 (API 24), cél API 37
- **Felépítés:** egyetlen Activity és több Fragment
- **Felületek:** ConstraintLayout, Material Components
- **Navigáció:** alsó navigációs sáv (Vonalak, Megállók) és eszköztár (Action Bar)
- **Listák:** RecyclerView
- **Hálózat:** Retrofit a menetrend API eléréséhez
- **Képek:** Glide a nagyfelbontású képek betöltéséhez
