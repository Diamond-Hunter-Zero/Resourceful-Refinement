The ExtendedHeatCondition enum is an extension of Create's HeatCondition class. In addition to the existing 'none', 'heated', and 'superheated' values, ExtendedHeatCondition adds the 'chilled', 'cooled', and 'passive' (equivalent to passive heating values used for boiler data) heat values.

| **ID**      | **Color**  | **Heat Level Num (Equivalent to BoilerHeater interface values)** |
| ----------- | ---------- | ---------------------------------------------------------------- |
| chilled     | 0xBFF2F5   | <= -3                                                            |
| cooled      | 0x1A83C9   | -2                                                               |
| none        | 16777215   | -1                                                               |
| passive     | 0xFF86C43B | 0                                                                |
| heated      | 15237888   | 1                                                                |
| superheated | 6067176    | >= 2                                                             |
 Conversions between these enum values and raw heat values are defined in the HeatUtilities.java class.