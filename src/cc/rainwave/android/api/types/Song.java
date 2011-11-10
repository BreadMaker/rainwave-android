package cc.rainwave.android.api.types;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
	public int song_id, elec_entry_id, elec_isrequest;
	public String song_title;
	public Artist artists[];
	public String album_art;
	public String album_name;
	public String song_requestor;
	public float song_rating_user, song_rating_avg,
		album_rating_user, album_rating_avg;
	
	private Song(Parcel in) {
		song_id = in.readInt();
		elec_entry_id = in.readInt();
		elec_isrequest = in.readInt();
	    song_title = in.readString();
	    Parcelable tmp[] = in.readParcelableArray(Artist[].class.getClassLoader());
	    album_art = in.readString();
	    album_name = in.readString();
	    song_requestor = in.readString();
	    
	    artists = new Artist[tmp.length];
	    for(int i = 0; i < tmp.length; i++) {
	        artists[i] = (Artist) tmp[i];
	    }
	}
	
	public boolean isRequest() {
		switch(elec_isrequest) {
		case ELEC_FULFILLED_REQUEST:
		case ELEC_RANDOM_REQUEST:
			return true;
		}
		return false;
	}
	
	public String collapseArtists() {
	    return collapseArtists(", ", " & ");
	}
	
	public String collapseArtists(String comma, String and) {
		if(artists == null) return "";
	    switch(artists.length) {
	        case 0: return "???";
	        case 1: return artists[0].artist_name;
	        case 2: return artists[0].artist_name + " " + and + " " + artists[1].artist_name;
	        default:
	            StringBuilder sb = new StringBuilder();
	            for(int i = 0; i < artists.length; i++) {
	                sb.append(artists[i].artist_name);
	                
	                if(i < artists.length - 2) {
	                    sb.append(comma);
	                }
	                else if(i == artists.length - 2) {
	                    sb.append(and);
	                }
	            }
	            return sb.toString();
	    }
	}
	
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeInt(song_id);
    	dest.writeInt(elec_entry_id);
    	dest.writeInt(elec_isrequest);
        dest.writeString(song_title);
        dest.writeParcelableArray(artists, flags);
        dest.writeString(album_art);
        dest.writeString(album_name);
        dest.writeString(song_requestor);
    }
    
    public static final Parcelable.Creator<Song> CREATOR
    = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
    
    /** LiquidRain's definition for the field 'elec_isrequest' */
    public static final int
    	ELEC_FULFILLED_REQUEST = 4,
    	ELEC_RANDOM_REQUEST = 3,
    	ELEC_NORMAL = 2,
    	ELEC_CONFLICT = 0;
}
