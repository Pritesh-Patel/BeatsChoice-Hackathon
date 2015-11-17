package models

import play.api.libs.json.Json
/**
 * Created by Pritesh on 21/03/2015.
 */
case class Track(id:String, title:String, artist:String, mp3URL:String, albumArtURL:String)

object Track
{
  implicit val trackFormat = Json.format[Track]
  implicit val trackWriter = Json.writes[Track]
}