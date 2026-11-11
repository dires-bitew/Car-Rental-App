package com.example.data

import com.example.model.GeoPoint

data class RouteWaypoint(
    val point: GeoPoint,
    val speedLimitMph: Float,
    val instruction: String,
    val distanceToNextMeters: Float
)

object SimulationRoute {
    // Beautiful coastal scenic loop along Great Highway & Skyline Drive
    val waypoints: List<RouteWaypoint> = listOf(
        RouteWaypoint(
            GeoPoint(37.7749, -122.4194, headingDeg = 270f, streetName = "Market St & 7th"),
            speedLimitMph = 35f,
            instruction = "Head west on Market Street toward Civic Center",
            distanceToNextMeters = 500f
        ),
        RouteWaypoint(
            GeoPoint(37.7725, -122.4312, headingDeg = 265f, streetName = "Duboce Ave"),
            speedLimitMph = 40f,
            instruction = "Continue straight onto Duboce Ave",
            distanceToNextMeters = 800f
        ),
        RouteWaypoint(
            GeoPoint(37.7680, -122.4460, headingDeg = 240f, streetName = "Buena Vista Way"),
            speedLimitMph = 45f,
            instruction = "In 400m, bear left toward Portola Drive",
            distanceToNextMeters = 1100f
        ),
        RouteWaypoint(
            GeoPoint(37.7490, -122.4600, headingDeg = 210f, streetName = "Portola Dr"),
            speedLimitMph = 55f,
            instruction = "Merge onto Portola Dr Express corridor",
            distanceToNextMeters = 1400f
        ),
        RouteWaypoint(
            GeoPoint(37.7320, -122.4790, headingDeg = 190f, streetName = "Sloat Blvd & 19th Ave"),
            speedLimitMph = 60f,
            instruction = "Keep right to connect to Great Highway",
            distanceToNextMeters = 1600f
        ),
        RouteWaypoint(
            GeoPoint(37.7210, -122.5020, headingDeg = 280f, streetName = "Great Highway Oceanway"),
            speedLimitMph = 65f,
            instruction = "Follow Great Highway along Pacific Coastline",
            distanceToNextMeters = 2200f
        ),
        RouteWaypoint(
            GeoPoint(37.7450, -122.5080, headingDeg = 355f, streetName = "Ocean Beach Coastal Corridor"),
            speedLimitMph = 70f,
            instruction = "Cruise along Pacific Ocean Coastway",
            distanceToNextMeters = 2500f
        ),
        RouteWaypoint(
            GeoPoint(37.7760, -122.5110, headingDeg = 10f, streetName = "Cliff House Vista"),
            speedLimitMph = 50f,
            instruction = "Slow down for scenic Cliff House viewpoint",
            distanceToNextMeters = 1200f
        ),
        RouteWaypoint(
            GeoPoint(37.7890, -122.4850, headingDeg = 75f, streetName = "Lincoln Blvd (Presidio)"),
            speedLimitMph = 45f,
            instruction = "Enter Presidio Forest Scenic Route",
            distanceToNextMeters = 1800f
        ),
        RouteWaypoint(
            GeoPoint(37.8020, -122.4660, headingDeg = 45f, streetName = "Golden Gate Vista Point"),
            speedLimitMph = 55f,
            instruction = "Approaching Golden Gate Overlook",
            distanceToNextMeters = 1500f
        ),
        RouteWaypoint(
            GeoPoint(37.8080, -122.4400, headingDeg = 105f, streetName = "Marina Blvd & Yacht Harbor"),
            speedLimitMph = 40f,
            instruction = "Continue east along Marina Boulevard",
            distanceToNextMeters = 1300f
        ),
        RouteWaypoint(
            GeoPoint(37.7950, -122.4020, headingDeg = 180f, streetName = "Embarcadero Waterfront"),
            speedLimitMph = 40f,
            instruction = "Follow Embarcadero toward Ferry Building",
            distanceToNextMeters = 1000f
        )
    )
}
