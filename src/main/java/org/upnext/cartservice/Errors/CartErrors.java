package org.upnext.cartservice.Errors;
import org.upnext.sharedlibrary.Errors.Error;
public class CartErrors {
    public static final Error CartNotFound = new Error("Cart.NotFound", "Cart Not Found", 404);
}
