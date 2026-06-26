package domain.entities;

import domain.valueObjects.OrderType;

public enum OrderState implements IOrderState {
    Registered {
        @Override
        public boolean Next(CarOrder context) {
            if (context.getOrderType() == OrderType.Premade) {
                context.SetState(ManagerAgreed);
            } else {
                context.SetState(StorageAgreed);
            }

            return true;
        }
        @Override
        public boolean Cancel(CarOrder context) {
            context.SetState(Canceled);
            return true;
        }
    },

    ManagerAgreed {
        @Override
        public boolean Next(CarOrder context) {
            context.SetState(WaitPayment);
            return true;
        }

        @Override
        public boolean Cancel(CarOrder context) {
            context.SetState(Canceled);
            return true;
        }
    },
    StorageAgreed {
        @Override
        public boolean Next(CarOrder context) {
            context.SetState(WaitPayment);
            return true;
        }

        @Override
        public boolean Cancel(CarOrder context) {
            context.SetState(Canceled);
            return true;
        }
    },

    WaitPayment {
        @Override
        public boolean Next(CarOrder context) {
            context.SetState(Paid);
            return true;
        }

        @Override
        public boolean Cancel(CarOrder context) {
            context.SetState(Canceled);
            return true;
        }
    },
    Paid {
        @Override
        public boolean Next(CarOrder context) {
            if (context.getOrderType() == OrderType.Premade) {
                context.SetState(CarReady);
            } else {
                context.SetState(WaitCarDelivery);
            }
            return true;
        }

        @Override
        public boolean Cancel(CarOrder context) {
            context.SetState(Canceled);
            return true;
        }
    },

    WaitCarDelivery {
        @Override
        public boolean Next(CarOrder context) {
            context.SetState(CarReady);
            return true;
        }
    },

    CarReady {
        @Override
        public boolean Next(CarOrder context) {
            context.SetState(Done);
            return true;
        }
    },
    Done {},
    Canceled {};

    @Override
    public boolean Next(CarOrder context) {
        return false;
    }
    @Override
    public boolean Cancel(CarOrder ctx) {
        return false;
    }

}
