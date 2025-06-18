/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.fdx.injector.ActivityTools;
public interface IProxyControl extends android.os.IInterface
{
  /** Default implementation for IProxyControl. */
  public static class Default implements com.fdx.injector.ActivityTools.IProxyControl
  {
    @Override public boolean start() throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean stop() throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean isRunning() throws android.os.RemoteException
    {
      return false;
    }
    @Override public int getPort() throws android.os.RemoteException
    {
      return 0;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.fdx.injector.ActivityTools.IProxyControl
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.fdx.injector.ActivityTools.IProxyControl interface,
     * generating a proxy if needed.
     */
    public static com.fdx.injector.ActivityTools.IProxyControl asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.fdx.injector.ActivityTools.IProxyControl))) {
        return ((com.fdx.injector.ActivityTools.IProxyControl)iin);
      }
      return new com.fdx.injector.ActivityTools.IProxyControl.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_start:
        {
          boolean _result = this.start();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_stop:
        {
          boolean _result = this.stop();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_isRunning:
        {
          boolean _result = this.isRunning();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_getPort:
        {
          int _result = this.getPort();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.fdx.injector.ActivityTools.IProxyControl
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public boolean start() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_start, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public boolean stop() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public boolean isRunning() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isRunning, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public int getPort() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPort, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_start = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_stop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_isRunning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getPort = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
  }
  public static final java.lang.String DESCRIPTOR = "com.fdx.injector.ActivityTools.IProxyControl";
  public boolean start() throws android.os.RemoteException;
  public boolean stop() throws android.os.RemoteException;
  public boolean isRunning() throws android.os.RemoteException;
  public int getPort() throws android.os.RemoteException;
}
