package serverControllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jdbc.DataBaseQuery;
import logic.Reservation;
import logic.SendObject;

public class cancelOldReservation implements Runnable {
	private DataBaseQuery con;

	/**
	 * Constructor of the class
	 * 
	 * @param con connection to database
	 */
	public cancelOldReservation(DataBaseQuery con) {
		this.con = con;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			LocalDateTime now = LocalDateTime.now();
			// If it's the 1st day of the month and around 00:00 (midnight)
			if (now.toLocalTime().isBefore(LocalTime.of(0, 1))) {
				List<Reservation> reservationList = new ArrayList<>();
				try {
					SendObject<?> result = SendObjectHandler.sendObjectHandle(new SendObject<String>("Get", "all reservations"), con);
					reservationList = (List<Reservation>) result.getObj();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				for (Reservation reservation : reservationList) {
					if(reservation.getStartTime()!=null&&reservation.getDate().isBefore(LocalDate.now())) {
						reservation.setStartTime(null);
						try {
							SendObjectHandler.sendObjectHandle(new SendObject<Reservation>("Update", reservation), con);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}		
					}
				}
				// Sleep for 1 day to avoid multiple triggers on the same day
				Thread.sleep(24 * 60 * 60 * 1000); // 24 hours
			} else {
				// Sleep for 1 hour and check again
				Thread.sleep(60 * 60 * 1000); // 1 hour
			}
		} catch (InterruptedException e) {
			
		}

	}

}
