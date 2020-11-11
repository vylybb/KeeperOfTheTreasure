# Keeper of the Treasure
A simple Java-Service that offers reports and alerts to monitor a trading-portfolio.
It is using Interactive-Brokers TWS.

## The origin 
This project was originally intended to meet the specific requirements of a client.
A subset of this functionality (plus some additional features) made its way then into the version you find in this repo.

I am thinking about enhancing it in the future.
If you have ideas for more features or other suggestions, just get in touch.
philipp.dev(ät)gmx.ch

## What state does the project have?
<span style="color:red">It is Work In Progress</span>

Represents the state after cutting out critical parts and refactorings.
\
Only run on Paper-Trading accounts, not tested / tests are not migrated yet.

## Features
+ Detects when a position looses its coverage (its protecting Stop-Loss, Trailing-Stop etc.), and sends a email-notification.
+ Persists costs (commissions etc.) withing one year. Thresholds can be defined so that when the total of costs exceeds them, a email-notification is being sent.
+ Thresholds on the TotLiquidationValue. A breach will trigger a notification.
+ Emergency exit. Total liquidation of all position in case the TotalLiquidationValue reaches a limit.
+ Simple HTML-Report showing the distribution of risk over the individual positions.

## Installation instructions
 1. Install MongoDB, TWS / IBGateway
 1. Get a api-key at [exchangerate-api.com](http:exchangerate-api.com)
 1. gitclone this project
 1. Fill in the gaps in the application.properties-file
 1. Use Gradle and start
 
 ## Restrictions
 + IB only offers cancellation of orders at the access-point where they were produced.
 + Loss of connection will lead to gaps in the persisted data so corresponding alerts wont be accurate.

## Warranty / License 
No Warranty, No License, I do not guarantee the functionality  
