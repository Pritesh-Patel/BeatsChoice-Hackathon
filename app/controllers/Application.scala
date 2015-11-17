package controllers

import java.util

import Services.{Music, Mp3Helper}
import akka.actor.{Props, ActorRef}
import akka.io.Tcp.Connected
import models.{Room, Track}
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.Cursor
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.collection.JavaConverters._
import scala.collection.mutable

import scala.collection.mutable.HashMap
import scala.concurrent.Future
import scala.util.Random

object Application extends Controller with MongoController {

  def rooms: JSONCollection = db.collection[JSONCollection]("rooms")

  val roomTracking:HashMap[String,String] = HashMap()
  val existingRooms:scala.collection.mutable.MutableList[String] = scala.collection.mutable.MutableList[String]()
  def index = Action
  {
    Ok(views.html.index("yo"))
  }

  def room(roomName:String,room:String) =
   {
     if(room == "create" && !existingRooms.contains(roomName))
     {

      createRoom(roomName)

     }else
     {
       if(existingRooms.contains(roomName) && room== "create")
       joinRoomAdmin(roomName)
       else joinRoom(roomName)
     }

  }

  def createRoom(roomName:String) = Action{

      existingRooms += roomName
      val vTracks: List[Track] = randomTracks()
      System.out.println(vTracks)
      val ct: Track = randomTracks().head

      val votes = Map("1" -> 0, "2" -> 0, "3" -> 0)
      val newRoom: Room = Room(None, roomName, ct, vTracks, votes)
      System.out.println(newRoom)

      rooms.save(newRoom)
      roomTracking.put(roomName, ct.title)
      System.out.println("create: " + roomTracking.get(roomName).get)
      Ok(views.html.room(newRoom, true))


  }

  def joinRoom(roomName:String) = Action.async{
    //find room

      val cursor:Cursor[Room] = rooms.find(Json.obj("roomName" -> roomName)).cursor[Room]
      val roomsFuture:Future[List[Room]] = cursor.collect[List]()
      roomsFuture.map{
        froom => Ok(views.html.room(froom.head,false))
      }
  }

  def joinRoomAdmin(roomName:String) = Action.async{
    //find room

    val cursor:Cursor[Room] = rooms.find(Json.obj("roomName" -> roomName)).cursor[Room]
    val roomsFuture:Future[List[Room]] = cursor.collect[List]()
    roomsFuture.map{
      froom => Ok(views.html.room(froom.head,true))
    }
  }





    def vote(id:String, roomName:String) = Action.async
  {
    val cursor:Cursor[Room] = rooms.find(Json.obj("roomName" -> roomName)).cursor[Room]
    val roomsFuture:Future[List[Room]] = cursor.collect[List]()
    roomsFuture.map{
      froom => {

        val map = froom.head.votes
        val newV:Int = map.get(id).get +1
        froom.head.votes =map + (id -> newV)

        rooms.save(froom.head)
        Ok("Recieved")

      }
    }




  }

  def songDone(roomName:String) = Action.async
  {
    val cursor:Cursor[Room] = rooms.find(Json.obj("roomName" -> roomName)).cursor[Room]
    val roomsFuture:Future[List[Room]] = cursor.collect[List]()
    roomsFuture.map{
      froom => {

        val map = froom.head.votes
        val winner:(String,Int) = map.maxBy(_._2)
        System.out.println(winner)
        val ct:Track = froom.head.votingTracks.filter(_.id == winner._1).head
        System.out.println(ct)

        val votes = Map("1" -> 0, "2"->0, "3"->0)
        val newRoom:Room = Room(froom.head._id,roomName,ct,randomTracks(),votes)
        rooms.save(newRoom)
        roomTracking.put(roomName,ct.title)

        Ok(views.html.room(newRoom,true))


      }
    }
  }

  def randomTracks():List[Track] = {

    val list:scala.collection.mutable.MutableList[Track] = scala.collection.mutable.MutableList[Track]()

    val jList:List[Music] = Mp3Helper.proccesAllMusic().asScala.toList
    val shuffle:List[Music] = Random.shuffle(jList)

    for(x <- 1 to 3)
    {
      val m:Music = shuffle(x)
      val name:String = m.name
      val artist:String = m.artist
      val mp3URL:String = m.mp3URL
      val imgURL:String = m.imageFile

      list+=Track(x.toString,name,artist,mp3URL, imgURL)
    }



    list.toList


  }


  def songCheck(roomName:String, cTrack:String) = Action
  {
    System.out.println("roomnamefromadil" + roomName);

    System.out.println(roomTracking.getOrElse(roomName,null) +" AND" + cTrack);

    if(roomTracking.get(roomName).get != cTrack)
    {
      Ok("reload")

    }else
    {
      Ok("nothing")
    }
  }







}