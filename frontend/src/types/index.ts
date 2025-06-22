export interface ParkingLot {
  id: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  totalSpaces: number;
  availableSpaces: number;
  hourlyRate: number;
  isOpen: boolean;
  distance?: number;
  imageUrl?: string;
}

export interface Reservation {
  id: string;
  parkingLotId: string;
  parkingLotName: string;
  startTime: string;
  endTime: string;
  duration: number; // in hours
  totalAmount: number;
  status: 'pending' | 'confirmed' | 'cancelled' | 'completed';
  createdAt: string;
}

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  carNumber?: string;
}

export interface PaymentMethod {
  id: string;
  type: 'credit_card' | 'debit_card' | 'digital_wallet';
  last4?: string;
  brand?: string;
  isDefault: boolean;
}

export interface Location {
  latitude: number;
  longitude: number;
} 