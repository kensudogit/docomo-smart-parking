import axios from 'axios';
import { ParkingLot, Reservation, User, PaymentMethod } from '../types';

const API_BASE_URL = 'http://localhost:3000/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 駐車場関連のAPI
export const parkingApi = {
  // 近くの駐車場を検索
  searchNearby: async (latitude: number, longitude: number, radius: number = 5000) => {
    const response = await api.get('/parking-lots/nearby', {
      params: { latitude, longitude, radius },
    });
    return response.data as ParkingLot[];
  },

  // 駐車場詳細を取得
  getById: async (id: string) => {
    const response = await api.get(`/parking-lots/${id}`);
    return response.data as ParkingLot;
  },

  // 満空情報を取得
  getAvailability: async (id: string) => {
    const response = await api.get(`/parking-lots/${id}/availability`);
    return response.data;
  },
};

// 予約関連のAPI
export const reservationApi = {
  // 予約を作成
  create: async (data: {
    parkingLotId: string;
    startTime: string;
    endTime: string;
    carNumber?: string;
  }) => {
    const response = await api.post('/reservations', data);
    return response.data as Reservation;
  },

  // ユーザーの予約一覧を取得
  getUserReservations: async () => {
    const response = await api.get('/reservations');
    return response.data as Reservation[];
  },

  // 予約をキャンセル
  cancel: async (id: string) => {
    const response = await api.put(`/reservations/${id}/cancel`);
    return response.data as Reservation;
  },

  // 予約詳細を取得
  getById: async (id: string) => {
    const response = await api.get(`/reservations/${id}`);
    return response.data as Reservation;
  },
};

// 決済関連のAPI
export const paymentApi = {
  // 決済方法を取得
  getPaymentMethods: async () => {
    const response = await api.get('/payment-methods');
    return response.data as PaymentMethod[];
  },

  // 決済を実行
  processPayment: async (data: {
    reservationId: string;
    paymentMethodId: string;
    amount: number;
  }) => {
    const response = await api.post('/payments', data);
    return response.data;
  },

  // 決済方法を追加
  addPaymentMethod: async (data: {
    type: string;
    token: string;
  }) => {
    const response = await api.post('/payment-methods', data);
    return response.data as PaymentMethod;
  },
};

// ユーザー関連のAPI
export const userApi = {
  // ユーザー情報を取得
  getProfile: async () => {
    const response = await api.get('/user/profile');
    return response.data as User;
  },

  // ユーザー情報を更新
  updateProfile: async (data: Partial<User>) => {
    const response = await api.put('/user/profile', data);
    return response.data as User;
  },
};

export default api; 