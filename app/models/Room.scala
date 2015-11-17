package models

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID


/**
 * Created by Pritesh on 21/03/2015.
 */
case class Room(_id: Option[BSONObjectID],roomName:String, currentTrack:Track, votingTracks:List[Track], var votes:Map[String,Int])

object Room
{
  implicit val roomFormat = Json.format[Room]
  implicit val roomWriter = Json.writes[Room]
}