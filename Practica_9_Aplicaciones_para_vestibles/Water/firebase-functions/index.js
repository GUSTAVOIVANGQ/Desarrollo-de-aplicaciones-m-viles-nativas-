// Firebase Cloud Functions para envío de notificaciones de hidratación
// Este archivo debe ser desplegado en Firebase Functions

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Función para enviar recordatorio de hidratación a un usuario específico
 */
exports.sendHydrationReminder = functions.https.onCall(async (data, context) => {
  try {
    // Verificar autenticación
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'Usuario no autenticado');
    }

    const { targetUserId, title, body, senderUserId, senderName, type, timestamp, data: notificationData } = data;

    if (!targetUserId || !title || !body) {
      throw new functions.https.HttpsError('invalid-argument', 'Parámetros requeridos faltantes');
    }

    // Obtener el token FCM del usuario objetivo
    const tokenDoc = await admin.firestore().collection('device_tokens').doc(targetUserId).get();
    
    if (!tokenDoc.exists) {
      console.log(`No se encontró token para usuario: ${targetUserId}`);
      return { success: false, message: 'Usuario no tiene dispositivo registrado' };
    }

    const tokenData = tokenDoc.data();
    const fcmToken = tokenData.fcmToken;

    if (!fcmToken) {
      console.log(`Token FCM vacío para usuario: ${targetUserId}`);
      return { success: false, message: 'Token FCM no válido' };
    }

    // Construir el mensaje de notificación
    const message = {
      token: fcmToken,
      notification: {
        title: title,
        body: body,
      },
      data: {
        type: type || 'hydration_reminder',
        senderUserId: senderUserId || '',
        senderName: senderName || '',
        timestamp: timestamp ? timestamp.toString() : Date.now().toString(),
        reminderMessage: notificationData?.reminderMessage || '',
        senderIntake: notificationData?.senderIntake ? notificationData.senderIntake.toString() : '0',
        senderGoal: notificationData?.senderGoal ? notificationData.senderGoal.toString() : '2000',
      },
      android: {
        priority: 'high',
        notification: {
          icon: 'ic_water_drop',
          color: '#2196F3',
          sound: 'default',
          channelId: 'HYDRATION_CHANNEL'
        }
      }
    };

    // Enviar la notificación
    const response = await admin.messaging().send(message);
    console.log('Notificación enviada exitosamente:', response);

    // Guardar registro en Firestore
    await admin.firestore().collection('notification_logs').add({
      targetUserId,
      senderUserId,
      type,
      title,
      body,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      fcmResponse: response,
      status: 'sent'
    });

    return { success: true, messageId: response };

  } catch (error) {
    console.error('Error enviando notificación:', error);
    
    // Guardar error en logs
    await admin.firestore().collection('notification_logs').add({
      targetUserId: data.targetUserId || '',
      senderUserId: data.senderUserId || '',
      type: data.type || 'hydration_reminder',
      title: data.title || '',
      body: data.body || '',
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      error: error.message,
      status: 'error'
    });

    throw new functions.https.HttpsError('internal', `Error enviando notificación: ${error.message}`);
  }
});

/**
 * Función para enviar recordatorio de hidratación a un grupo de usuarios
 */
exports.sendGroupHydrationReminder = functions.https.onCall(async (data, context) => {
  try {
    // Verificar autenticación
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'Usuario no autenticado');
    }

    const { targetUserIds, title, body, senderUserId, senderName, groupId, groupName, type, timestamp, data: notificationData } = data;

    if (!targetUserIds || !Array.isArray(targetUserIds) || targetUserIds.length === 0) {
      throw new functions.https.HttpsError('invalid-argument', 'Lista de usuarios objetivo requerida');
    }

    if (!title || !body) {
      throw new functions.https.HttpsError('invalid-argument', 'Título y mensaje requeridos');
    }

    const results = [];
    const errors = [];

    // Enviar notificación a cada usuario del grupo
    for (const targetUserId of targetUserIds) {
      try {
        // Obtener el token FCM del usuario
        const tokenDoc = await admin.firestore().collection('device_tokens').doc(targetUserId).get();
        
        if (!tokenDoc.exists) {
          errors.push(`No se encontró token para usuario: ${targetUserId}`);
          continue;
        }

        const tokenData = tokenDoc.data();
        const fcmToken = tokenData.fcmToken;

        if (!fcmToken) {
          errors.push(`Token FCM vacío para usuario: ${targetUserId}`);
          continue;
        }

        // Construir el mensaje de notificación
        const message = {
          token: fcmToken,
          notification: {
            title: title,
            body: body,
          },
          data: {
            type: type || 'group_hydration_reminder',
            senderUserId: senderUserId || '',
            senderName: senderName || '',
            groupId: groupId || '',
            groupName: groupName || '',
            timestamp: timestamp ? timestamp.toString() : Date.now().toString(),
            reminderMessage: notificationData?.reminderMessage || '',
            senderIntake: notificationData?.senderIntake ? notificationData.senderIntake.toString() : '0',
            senderGoal: notificationData?.senderGoal ? notificationData.senderGoal.toString() : '2000',
          },
          android: {
            priority: 'high',
            notification: {
              icon: 'ic_water_drop',
              color: '#4CAF50',
              sound: 'default',
              channelId: 'HYDRATION_CHANNEL'
            }
          }
        };

        // Enviar la notificación
        const response = await admin.messaging().send(message);
        results.push({ userId: targetUserId, messageId: response, status: 'sent' });

        // Guardar registro individual
        await admin.firestore().collection('notification_logs').add({
          targetUserId,
          senderUserId,
          groupId,
          type,
          title,
          body,
          timestamp: admin.firestore.FieldValue.serverTimestamp(),
          fcmResponse: response,
          status: 'sent'
        });

      } catch (userError) {
        console.error(`Error enviando a usuario ${targetUserId}:`, userError);
        errors.push(`Error para usuario ${targetUserId}: ${userError.message}`);
        
        // Guardar error individual
        await admin.firestore().collection('notification_logs').add({
          targetUserId,
          senderUserId,
          groupId,
          type,
          title,
          body,
          timestamp: admin.firestore.FieldValue.serverTimestamp(),
          error: userError.message,
          status: 'error'
        });
      }
    }

    console.log(`Notificaciones enviadas: ${results.length}, Errores: ${errors.length}`);

    return { 
      success: results.length > 0,
      results,
      errors,
      totalSent: results.length,
      totalErrors: errors.length
    };

  } catch (error) {
    console.error('Error enviando notificación grupal:', error);
    throw new functions.https.HttpsError('internal', `Error enviando notificación grupal: ${error.message}`);
  }
});

/**
 * Función para enviar notificación cuando se alcanza una meta
 */
exports.sendGoalAchievedNotification = functions.https.onCall(async (data, context) => {
  try {
    // Verificar autenticación
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'Usuario no autenticado');
    }

    const { targetUserIds, title, body, senderUserId, senderName, type, timestamp, data: notificationData } = data;

    if (!targetUserIds || !Array.isArray(targetUserIds) || targetUserIds.length === 0) {
      throw new functions.https.HttpsError('invalid-argument', 'Lista de usuarios objetivo requerida');
    }

    const results = [];
    const errors = [];

    // Enviar notificación a cada usuario
    for (const targetUserId of targetUserIds) {
      try {
        // Obtener el token FCM del usuario
        const tokenDoc = await admin.firestore().collection('device_tokens').doc(targetUserId).get();
        
        if (!tokenDoc.exists) {
          errors.push(`No se encontró token para usuario: ${targetUserId}`);
          continue;
        }

        const tokenData = tokenDoc.data();
        const fcmToken = tokenData.fcmToken;

        if (!fcmToken) {
          errors.push(`Token FCM vacío para usuario: ${targetUserId}`);
          continue;
        }

        // Construir el mensaje de notificación
        const message = {
          token: fcmToken,
          notification: {
            title: title,
            body: body,
          },
          data: {
            type: type || 'goal_achieved',
            senderUserId: senderUserId || '',
            senderName: senderName || '',
            timestamp: timestamp ? timestamp.toString() : Date.now().toString(),
            achievement: notificationData?.achievement || 'daily_goal',
            intake: notificationData?.intake ? notificationData.intake.toString() : '0',
            goal: notificationData?.goal ? notificationData.goal.toString() : '2000',
          },
          android: {
            priority: 'high',
            notification: {
              icon: 'ic_water_drop',
              color: '#FF9800',
              sound: 'default',
              channelId: 'HYDRATION_CHANNEL'
            }
          }
        };

        // Enviar la notificación
        const response = await admin.messaging().send(message);
        results.push({ userId: targetUserId, messageId: response, status: 'sent' });

      } catch (userError) {
        console.error(`Error enviando a usuario ${targetUserId}:`, userError);
        errors.push(`Error para usuario ${targetUserId}: ${userError.message}`);
      }
    }

    return { 
      success: results.length > 0,
      results,
      errors,
      totalSent: results.length,
      totalErrors: errors.length
    };

  } catch (error) {
    console.error('Error enviando notificación de meta alcanzada:', error);
    throw new functions.https.HttpsError('internal', `Error enviando notificación: ${error.message}`);
  }
});

/**
 * Función para enviar notificaciones programadas (cron job)
 */
exports.sendScheduledHydrationReminders = functions.pubsub.schedule('every 2 hours').onRun(async (context) => {
  try {
    console.log('Iniciando envío de recordatorios programados...');

    // Obtener todos los usuarios activos
    const usersSnapshot = await admin.firestore().collection('users').where('isActive', '==', true).get();
    
    if (usersSnapshot.empty) {
      console.log('No hay usuarios activos para enviar recordatorios');
      return null;
    }

    const messages = [
      '💧 ¡Es hora de beber agua!',
      '🚰 Recordatorio: hidrátate',
      '💪 Tu cuerpo necesita agua',
      '⏰ Hora de hidratación',
      '🌊 Mantente hidratado'
    ];

    const randomMessage = messages[Math.floor(Math.random() * messages.length)];
    const results = [];

    // Enviar a cada usuario
    for (const userDoc of usersSnapshot.docs) {
      try {
        const userId = userDoc.id;
        
        // Obtener token del dispositivo
        const tokenDoc = await admin.firestore().collection('device_tokens').doc(userId).get();
        
        if (!tokenDoc.exists) continue;
        
        const tokenData = tokenDoc.data();
        const fcmToken = tokenData.fcmToken;
        
        if (!fcmToken) continue;

        const message = {
          token: fcmToken,
          notification: {
            title: '💧 Recordatorio de Hidratación',
            body: randomMessage,
          },
          data: {
            type: 'scheduled_reminder',
            timestamp: Date.now().toString(),
          },
          android: {
            priority: 'normal',
            notification: {
              icon: 'ic_water_drop',
              color: '#2196F3',
              sound: 'default',
              channelId: 'HYDRATION_CHANNEL'
            }
          }
        };

        const response = await admin.messaging().send(message);
        results.push({ userId, messageId: response });

      } catch (userError) {
        console.error(`Error enviando recordatorio programado a usuario:`, userError);
      }
    }

    console.log(`Recordatorios programados enviados: ${results.length}`);
    return { success: true, sent: results.length };

  } catch (error) {
    console.error('Error en recordatorios programados:', error);
    return { success: false, error: error.message };
  }
});
