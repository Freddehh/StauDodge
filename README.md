# StauDodge
This is a small application that fetches the location from the user and then uses that to fetch incident reports from Sveriges Radio traffic API. (https://sverigesradio.se/api/documentation/v2/metoder/trafik.html)
The application shows these incident rapports in a recycleview where each card is an incident. Each card contains information about priority, title, description and category. The cards can be sorted by priority if wanted.

# What could be improved
* I didn't manage to get the fusedLocationProviderClient to work which seems to be the go-to solution while fetching locations from users now so that should be implemented.
* The recycleview could be more interactive. It would be good to be able to remove the incident you don't need anymore. 
* It works since there aren't that many incidents at each location but it would be better to only remove or add the new incidents instead of replacing all if there's a change. 
* And of course, the overall look and feel of the application would have to be improved if this ever were to be released.  
