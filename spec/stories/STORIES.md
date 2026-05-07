# User Stories: Afterfall (U-Bahn Management)

Diese User Stories sind in logische Epics (Themenbereiche) unterteilt und so geschnitten, dass sie in überschaubaren Aufgaben (Sprints) abgearbeitet werden können.

## Epic 1: Grundlegendes Spielsystem & Tutorial
*Das Fundament des Spiels und der Erstkontakt für den Spieler.*

* **US 1.1 - Spielzeit & Pause:** Als Spieler möchte ich das Spiel jederzeit pausieren und fortsetzen können, um in Ruhe Entscheidungen zu treffen und mein Netz zu planen.
* **US 1.2 - Kamerasteuerung:** Als Spieler möchte ich die Karte frei bewegen und zoomen können, um die wachsende Stadt und mein gesamtes Streckennetz zu überblicken.
* **US 1.3 - Tutorial-Start:** Als neuer Spieler möchte ich zu Beginn zwei vorgebaute Stationen und einen kostenlosen Zug haben, um die Grundmechaniken ohne Druck kennenzulernen.
* **US 1.4 - Tutorial-Abschluss:** Als neuer Spieler möchte ich eine erste Aufgabe erhalten (die beiden Startstationen verbinden), deren erfolgreicher Abschluss mir signalisiert, dass das Endlosspiel jetzt richtig beginnt.

## Epic 2: Infrastruktur-Bau & Karteninteraktion
*Alles rund um das Bauen und Modifizieren der Spielwelt.*

* **US 2.1 - Stationen bauen:** Als Spieler möchte ich an beliebigen, freien Stellen auf der Karte neue Stationen platzieren können, um neue Stadtteile zu erschließen.
* **US 2.2 - Streckenbau:** Als Spieler möchte ich zwei Stationen mit Schienen verbinden können, wobei sich Strecken problemlos (ohne Kollision/Signale) kreuzen dürfen, damit der Bau unkompliziert bleibt.
* **US 2.3 - Baukosten:** Als Spieler möchte ich, dass der Bau von Stationen und Schienen Geld kostet und direkt von meinem Kontostand abgezogen wird, um wirtschaftlich planen zu müssen.
* **US 2.4 - Infrastruktur verkaufen:** Als Spieler möchte ich bestehende Stationen und Schienen abreißen (verkaufen) können, um mein Netz umzubauen oder bei Geldnot Kapital freizumachen.
* **US 2.5 - Bauverbot bei Schulden:** Als Spieler möchte ich keine neuen Gebäude oder Strecken bauen können, solange mein Kontostand im Minus ist.

## Epic 3: Linien- und Zugmanagement
*Routenplanung und Zugauslastung.*

* **US 3.1 - Linien erstellen:** Als Spieler möchte ich eine neue Route (Linie) erstellen können, indem ich mehrere Stationen nacheinander auswähle. Die Route soll automatisch eine gut sichtbare Farbe erhalten.
* **US 3.2 - Züge kaufen:** Als Spieler möchte ich aus drei verschiedenen Zugtypen (Standard, Mittel, Super) wählen und diese kaufen können, um auf unterschiedliche Passagiermengen reagieren zu können.
* **US 3.3 - Zugzuweisung & Automatik:** Als Spieler möchte ich meine Züge bestimmten Routen zuweisen. Die Züge sollen diese Routen anschließend automatisch abfahren.
* **US 3.4 - Betriebskosten:** Als Spieler möchte ich sehen, dass der Betrieb einer Route laufende Kosten (abhängig von Zugtyp und Streckenlänge) verursacht, damit ich ineffiziente Linien optimieren muss.
* **US 3.5 - Fahrplan-Verwaltung:** Als Spieler möchte ich Linien temporär deaktivieren können, um laufende Betriebskosten zu sparen, wenn ich im Minus bin.

## Epic 4: Passagiersimulation & Wegfindung
*Das Verhalten der Fahrgäste.*

* **US 4.1 - Passagier-Generierung:** Als Spieler möchte ich, dass die Stadt kontinuierlich Passagiere mit einem spezifischen Zielort generiert, die zu den Stationen laufen.
* **US 4.2 - Intelligente Wegfindung:** Als Spieler möchte ich, dass Passagiere automatisch den kürzesten Weg zu ihrem Ziel finden und dabei selbstständig zwischen verschiedenen Linien umsteigen.
* **US 4.3 - Darstellung an Stationen:** Als Spieler möchte ich an den Stationen sehen können, wie viele Passagiere dort warten, sortiert nach ihren Zielorten, um Engpässe zu erkennen.
* **US 4.4 - Boarding (Kapazitätslimit):** Als Spieler möchte ich, dass wartende Passagiere in eintreffende Züge einsteigen, bis die maximale Kapazität des jeweiligen Zugtyps erreicht ist. Die Stationen bieten unbegrenzt Platz für wartende Passagiere und unbegrenzt viele Gleise für Züge, aber volle Züge müssen die restlichen Passagiere am Bahnsteig zurücklassen.

## Epic 5: Wirtschaft & Ticketsystem
*Preise, Einnahmen und Net Worth.*

* **US 5.1 - Ticketpreise festlegen:** Als Spieler möchte ich den Ticketpreis pro gefahrener Station selbst definieren können, um die Balance zwischen Einnahmen und Attraktivität zu steuern.
* **US 5.2 - Einnahmen durch Fahrgäste:** Als Spieler möchte ich Geld verdienen, sobald ein Passagier sein Ziel erreicht (Ticketpreis x Anzahl der gefahrenen Stationen).
* **US 5.3 - Kontostand-UI:** Als Spieler möchte ich jederzeit meinen aktuellen Kontostand, meine laufenden Kosten und Einnahmen in einer Übersicht sehen.
* **US 5.4 - Net Worth (Unternehmenswert):** Als Spieler möchte ich, dass mein Unternehmenswert (Bargeld + Wert der Infrastruktur) kontinuierlich berechnet wird, um meinen Gesamterfolg zu messen.

## Epic 6: Stadtwachstum & Zufriedenheit
*Die dynamische Entwicklung der Stadt und die Bewertung des Spielers.*

* **US 6.1 - Dynamisches Stadtwachstum:** Als Spieler möchte ich, dass im Laufe der Zeit die Stadtbevölkerung wächst und neue Orte auf der Karte entstehen, die eine Anbindung fordern.
* **US 6.2 - Zufriedenheits-Berechnung:** Als Spieler möchte ich eine globale Prozentanzeige für die "Zufriedenheit der Stadt" sehen, die meine Leistung widerspiegelt.
* **US 6.3 - Auswirkungen auf Zufriedenheit:** Als Spieler möchte ich, dass sich hohe Wartezeiten, zu hohe Ticketpreise, überfüllte Züge und nicht-angebundene neue Orte negativ auf die Zufriedenheit auswirken.
* **US 6.4 - Nachfrage-Steuerung:** Als Spieler möchte ich, dass die aktuelle Zufriedenheit und der Ticketpreis direkten Einfluss auf die Generierungsrate neuer Passagiere haben (hohe Zufriedenheit/niedriger Preis = mehr Passagiere).

## Epic 7: Online Ranking
*Der kompetitive Langzeit-Faktor.*

* **US 7.1 - Ranglisten-Integration:** Als Spieler möchte ich meinen Unternehmenswert (Net Worth) in einer globalen oder Freundes-Rangliste vergleichen können, um motiviert zu bleiben, mein Netz immer weiter zu optimieren.
