package main.caroonline.service;

import lombok.AllArgsConstructor;

import main.caroonline.dto.*;
import main.caroonline.model.Room;
import main.caroonline.model.User;
import main.caroonline.storage.AppStorage;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AppConfigurationEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomService {
    public Room CreateRoom(CreateRoomRequest request) {
        Room room = new Room();
        room.setRoomID(UUID.randomUUID().toString());
        room.setRoomName(request.roomName);
        room.setRoomCategory(request.roomCategory);
        System.out.println(request.creatorId+".");
        room.setPlayer1(AppStorage.getInstance().getUserByID(request.creatorId));
        room.setPlayerAvailable(1);
        room.setTurn(room.getPlayer1().getUserID());
        room.setState("Ready");
        AppStorage.getInstance().setGame(room);
        AppStorage.getInstance().getUsers().remove(request.creatorId);
        return  room;
    }

    public Room JoinRoom(JoinRoomRequest request) {
        var room = AppStorage.getInstance().getRoomByID(request.roomID);
        if (room == null || !room.getState().equals("Ready")) {
            return null;
        } else {
            if (room.getPlayer2().getUserID().compareTo("") == 0) {
                var u = AppStorage.getInstance().getUserByID(request.UserID.trim());
                System.out.println(AppStorage.getInstance().getUserByID(request.UserID.trim()).getUserID());
                room.setPlayer2(u);
                room.setPlayerAvailable(room.getPlayerAvailable() + 1);
            } else if (room.getPlayer3().getUserID().compareTo("") == 0) {
                var u = AppStorage.getInstance().getUserByID(request.UserID);
                room.setPlayer3(u);
                room.setPlayerAvailable(room.getPlayerAvailable() + 1);
            } else return null;
        }
        AppStorage.getInstance().getGames().put(request.roomID, room);
        AppStorage.getInstance().getUsers().remove(request.UserID);
        return room;
    }
    public String leaveRoom(LeftRoomRequest request){
        System.out.println("Service: Leave Room");
        try {
            var room = AppStorage.getInstance().getRoomByID(request.RoomId);
            String name = null;
            if(room==null)
                return null;
            else {
                if(room.getPlayer1().getUserID().compareTo(request.UserId)==0){
                    name = room.getPlayer1().getName();
                    room.setPlayer1(room.getPlayer2());
                    room.setPlayer2(room.getPlayer3());
                    room.setTurn(room.getPlayer1().getUserID());
                    room.getPlayer3().setUserID("");
                }else if(room.getPlayer2().getUserID().compareTo(request.UserId)==0){
                    name = room.getPlayer2().getName();
                    room.setPlayer2(room.getPlayer3());
                    room.getPlayer3().setName("");
                }else if(room.getPlayer3().getUserID().compareTo(request.UserId)==0){
                    name = room.getPlayer3().getName();
                    room.getPlayer3().setUserID("");
                }
                room.setPlayerAvailable(room.getPlayerAvailable() - 1);
            }
            if(room.getPlayer1().getUserID().equals("")){
                AppStorage.getInstance().removeRoom(request.RoomId);
            }
            return name;
        }catch (Exception err){
            System.out.println("Loi khi xoa user khoi phong: "+request.RoomId);
            err.printStackTrace();
            return null;
        }
    }
    public User JoinPublicUserList(String userName){
        User user = new User();
        user.setName(userName);
        System.out.println(userName);
        user.setUserID(UUID.randomUUID().toString());
        AppStorage.getInstance().getUsers().put(user.getUserID(), user);
        return AppStorage.getInstance().getUserByID(user.getUserID());
    }
    public void LeavePublicUserList(String uid){
        AppStorage.getInstance().getUsers().remove(uid);
    }

    public User GetUserById(String id) {
        return AppStorage.getInstance().getUserByID(id);
    }

    public Room GetRoomById(String id) {
        return AppStorage.getInstance().getRoomByID(id);
    }

    public List<RoomInfoResponse> getPublicRoom() {
        List<Room> rooms = new ArrayList<>(AppStorage.getInstance().getGames().values());
        return rooms.stream().filter(room -> "public".equals(room.getRoomCategory())).map(room -> {
            RoomInfoResponse publicRoom = new RoomInfoResponse();
            publicRoom.setRoomId(room.getRoomID());
            publicRoom.setRoomName(room.getRoomName());
            publicRoom.setMemberInRoom(room.getPlayerAvailable());
            return publicRoom;
        }).collect(Collectors.toList());
    }
}
