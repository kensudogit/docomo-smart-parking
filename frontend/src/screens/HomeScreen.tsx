import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  RefreshControl,
  Alert,
} from 'react-native';
import {
  Card,
  Title,
  Paragraph,
  Button,
  ActivityIndicator,
  Chip,
  Text,
} from 'react-native-paper';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import Geolocation from 'react-native-geolocation-service';

import { parkingApi, reservationApi } from '../services/api';
import { ParkingLot, Reservation } from '../types';
import { RootStackParamList } from '../../App';

type HomeScreenNavigationProp = StackNavigationProp<RootStackParamList, 'MainTabs'>;

const HomeScreen = () => {
  const navigation = useNavigation<HomeScreenNavigationProp>();
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [nearbyParkingLots, setNearbyParkingLots] = useState<ParkingLot[]>([]);
  const [currentReservations, setCurrentReservations] = useState<Reservation[]>([]);
  const [userLocation, setUserLocation] = useState<{ latitude: number; longitude: number } | null>(null);

  const loadData = async () => {
    try {
      // 現在地を取得
      if (!userLocation) {
        Geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;
            setUserLocation({ latitude, longitude });
            fetchNearbyParkingLots(latitude, longitude);
          },
          (error) => {
            console.error('位置情報の取得に失敗しました:', error);
            Alert.alert('エラー', '位置情報の取得に失敗しました');
            setLoading(false);
          },
          { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 }
        );
      } else {
        await fetchNearbyParkingLots(userLocation.latitude, userLocation.longitude);
      }

      // 現在の予約を取得
      const reservations = await reservationApi.getUserReservations();
      setCurrentReservations(reservations.filter(r => r.status === 'confirmed'));
    } catch (error) {
      console.error('データの読み込みに失敗しました:', error);
      Alert.alert('エラー', 'データの読み込みに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const fetchNearbyParkingLots = async (latitude: number, longitude: number) => {
    try {
      const parkingLots = await parkingApi.searchNearby(latitude, longitude);
      setNearbyParkingLots(parkingLots.slice(0, 5)); // 上位5件を表示
    } catch (error) {
      console.error('駐車場情報の取得に失敗しました:', error);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'confirmed':
        return '#4caf50';
      case 'pending':
        return '#ff9800';
      case 'cancelled':
        return '#f44336';
      default:
        return '#757575';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'confirmed':
        return '予約済み';
      case 'pending':
        return '処理中';
      case 'cancelled':
        return 'キャンセル';
      default:
        return status;
    }
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#1976d2" />
        <Text style={styles.loadingText}>読み込み中...</Text>
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      {/* 現在の予約 */}
      {currentReservations.length > 0 && (
        <View style={styles.section}>
          <Title style={styles.sectionTitle}>現在の予約</Title>
          {currentReservations.map((reservation) => (
            <Card key={reservation.id} style={styles.card}>
              <Card.Content>
                <Title>{reservation.parkingLotName}</Title>
                <Paragraph>
                  開始: {new Date(reservation.startTime).toLocaleString()}
                </Paragraph>
                <Paragraph>
                  終了: {new Date(reservation.endTime).toLocaleString()}
                </Paragraph>
                <Paragraph>料金: ¥{reservation.totalAmount.toLocaleString()}</Paragraph>
                <Chip
                  mode="outlined"
                  textStyle={{ color: getStatusColor(reservation.status) }}
                  style={{ borderColor: getStatusColor(reservation.status), alignSelf: 'flex-start', marginTop: 8 }}
                >
                  {getStatusText(reservation.status)}
                </Chip>
              </Card.Content>
            </Card>
          ))}
        </View>
      )}

      {/* 近くの駐車場 */}
      <View style={styles.section}>
        <Title style={styles.sectionTitle}>近くの駐車場</Title>
        {nearbyParkingLots.map((parkingLot) => (
          <Card key={parkingLot.id} style={styles.card}>
            <Card.Content>
              <Title>{parkingLot.name}</Title>
              <Paragraph>{parkingLot.address}</Paragraph>
              <View style={styles.parkingInfo}>
                <Chip mode="outlined" style={styles.chip}>
                  空き: {parkingLot.availableSpaces}/{parkingLot.totalSpaces}
                </Chip>
                <Chip mode="outlined" style={styles.chip}>
                  ¥{parkingLot.hourlyRate}/時間
                </Chip>
                {parkingLot.distance && (
                  <Chip mode="outlined" style={styles.chip}>
                    {(parkingLot.distance / 1000).toFixed(1)}km
                  </Chip>
                )}
              </View>
              <View style={styles.buttonContainer}>
                <Button
                  mode="outlined"
                  onPress={() => navigation.navigate('ParkingDetail', { parkingId: parkingLot.id })}
                  style={styles.button}
                >
                  詳細を見る
                </Button>
                <Button
                  mode="contained"
                  onPress={() => navigation.navigate('Reservation', { parkingId: parkingLot.id })}
                  style={styles.button}
                  disabled={parkingLot.availableSpaces === 0}
                >
                  予約する
                </Button>
              </View>
            </Card.Content>
          </Card>
        ))}
      </View>

      {/* クイックアクション */}
      <View style={styles.section}>
        <Title style={styles.sectionTitle}>クイックアクション</Title>
        <View style={styles.quickActions}>
          <Button
            mode="contained"
            icon="search"
            onPress={() => navigation.navigate('Search')}
            style={styles.quickActionButton}
          >
            駐車場を検索
          </Button>
          <Button
            mode="outlined"
            icon="history"
            onPress={() => navigation.navigate('Profile')}
            style={styles.quickActionButton}
          >
            予約履歴
          </Button>
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
  },
  section: {
    margin: 16,
  },
  sectionTitle: {
    marginBottom: 12,
    fontSize: 20,
    fontWeight: 'bold',
  },
  card: {
    marginBottom: 12,
    elevation: 2,
  },
  parkingInfo: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 8,
  },
  chip: {
    marginRight: 8,
    marginBottom: 4,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 12,
  },
  button: {
    flex: 1,
    marginHorizontal: 4,
  },
  quickActions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  quickActionButton: {
    flex: 1,
    marginHorizontal: 4,
  },
});

export default HomeScreen; 