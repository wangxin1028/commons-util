package commons.util.db;
/**
 *
 *Author:WangXin69
 *Date:2018年5月2日
 *
 */
public class SerializerException extends RuntimeException{
	private static final long serialVersionUID = -4702880467007686044L;

	public SerializerException() {
		super();
	}

	public SerializerException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializerException(String message) {
		super(message);
	}

	public SerializerException(Throwable cause) {
		super(cause);
	}
}


