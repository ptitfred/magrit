Magrit
======

Presentation
------------

Magrit se veut un outil de build P2P basé sur git.
L'idée est de pouvoir demander la stabilité d'un commit avant de l'intégrer
ou de distribuer le build sur un ensemble de machines qui partagent le build.

Par exemple, si vous terminez une fonctionnalité composée de 5 commits et que 
la politique du projet demande à ce que tous les commits compilent et passent
les tests unitaires, vous devrez d'abord valider ces 5 commits.

Avec Magrit, vous pouvez proposer ces commits au réseau qui répartira les 5
builds sur les machines connectées et disponibles.

En parallèle, ce système vous permet de savoir où vous mettez les pieds si
vous intégrez des commits tiers via un rebase, cherry-pick ou apply. En effet
Magrit distribue aussi le résultat des builds des commits qu'il connait à
travers le réseau P2P ainsi constitué.
Si vous constatez qu'un commit a builder 2 fois avec succès sur des machines
différentes, vous pouvez raisonnablement l'intégrer.

Magrit n'impose pas la politique du projet. En revanche il étend git en
s'attaquant à la consistance du projet au niveau compilation et tests unitaires.
A noter que ce n'est qu'une proposition, vous pouvez choisir de ne garder que
la compilation comme condition d'acceptation. Le nombre de build ayant réussi
est également un paramètre que chaque équipe devra déterminer.


