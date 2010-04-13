package exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: jwmcwt6
 * Date: 30-mrt-2010
 * Time: 9:24:57
 * To change this template use File | Settings | File Templates.
 */
public class NotAuthenticatedException extends Exception{
  public NotAuthenticatedException() {
    super();
  }

  public NotAuthenticatedException(String msg) {
    super(msg);
  }
}
