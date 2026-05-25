# Prayer Cards

A simple Android application that displays 31 Prayer Cards for your everyday life.

## Features
- **Daily Prayers**: 31 unique prayers for each day of the month.
- **Prayer Status**: Track which prayers you have completed.
- **Monthly Reset**: Automatically resets prayer status at the beginning of each month.
- **Search Functionality**: Users can search for specific prayers, sources, or days within the collection.
- **Progress Restriction**: Prevents skipping ahead by only allowing users to mark prayers as "Done" for the current or past days.

## New Search Function
The latest update adds a search bar at the top of the main screen. This allows users to filter the prayer list by:
- Prayer content
- Source (Taken from)
- Day number

Simply type your query into the search bar, and the list will update in real-time.

## Technical Details
- Built with Java and Android SDK.
- Uses `RecyclerView` with a custom adapter.
- Data is stored in a JSON file (`data.json`) and persisted in internal storage after modifications.
