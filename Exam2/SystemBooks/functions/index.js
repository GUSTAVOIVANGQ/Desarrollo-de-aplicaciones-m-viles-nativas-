const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Función mejorada para enviar notificaciones con mejor rendimiento y confiabilidad
exports.sendNotification = functions
    .runWith({
        // Aumentar el tiempo de espera para evitar cortes en procesamiento masivo
        timeoutSeconds: 300,
        // Usar más memoria para procesamiento más rápido
        memory: '512MB'
    })
    .firestore
    .document('notifications/{notificationId}')
    .onCreate(async (snap, context) => {
        try {
            const notification = snap.data();
            const notificationId = context.params.notificationId;
            
            console.log(`Processing notification ${notificationId}:`, notification);
            
            if (!notification) {
                console.log('No notification data found');
                return null;
            }
            
            // Actualizar estado de la notificación para indicar que está en proceso
            await snap.ref.update({
                processingStartTime: admin.firestore.FieldValue.serverTimestamp(),
                status: 'processing'
            });
            
            const title = notification.title || 'SystemBooks';
            const body = notification.body || '';
            const data = notification.data || {};
            
            // Añadir el ID de la notificación a los datos para seguimiento
            const messageData = {
                ...data,
                notificationId: notificationId
            };
            
            // Si es para todos los usuarios
            if (notification.toAll) {
                console.log('Sending notification to all users');
                
                // Obtener todos los tokens FCM activos (últimos 30 días)
                const thirtyDaysAgo = Date.now() - (30 * 24 * 60 * 60 * 1000);
                const tokensSnapshot = await admin.firestore()
                    .collection('fcm_tokens')
                    .where('timestamp', '>', thirtyDaysAgo)
                    .get();
                    
                if (tokensSnapshot.empty) {
                    await updateNotificationStatus(snap.ref, 'failed', 'No devices found');
                    console.log('No devices found');
                    return null;
                }
                
                // Extraer los tokens, filtrando duplicados y nulos
                const tokens = [];
                const processedUserIds = new Set();
                
                tokensSnapshot.forEach(doc => {
                    const tokenData = doc.data();
                    const token = tokenData.token;
                    const userId = tokenData.userId;
                    
                    // Evitar tokens duplicados para el mismo usuario
                    if (token && userId && !processedUserIds.has(userId)) {
                        tokens.push(token);
                        processedUserIds.add(userId);
                    }
                });
                
                if (tokens.length === 0) {
                    await updateNotificationStatus(snap.ref, 'failed', 'No valid tokens found');
                    console.log('No valid tokens found');
                    return null;
                }
                
                console.log(`Sending to ${tokens.length} unique devices`);
                
                // Dividir en chunks de 500 (límite FCM)
                const tokensChunks = chunkArray(tokens, 500);
                let successCount = 0;
                let failureCount = 0;
                let invalidTokens = [];
                
                // Procesar cada chunk secuencialmente para evitar sobrecarga
                for (const tokensChunk of tokensChunks) {
                    // Preparar mensaje para este chunk
                    const message = {
                        notification: { 
                            title, 
                            body,
                            // Importante: añadir sonido y prioridad alta para entrega inmediata
                            sound: 'default'
                        },
                        data: messageData,
                        android: {
                            priority: 'high',
                            notification: {
                                sound: 'default',
                                channelId: 'SystemBooksChannel',
                                priority: 'high'
                            }
                        },
                        apns: {
                            payload: {
                                aps: {
                                    sound: 'default',
                                    contentAvailable: true
                                }
                            },
                            headers: {
                                'apns-push-type': 'alert',
                                'apns-priority': '10'
                            }
                        },
                        tokens: tokensChunk
                    };
                    
                    try {
                        // Enviar notificación y procesar resultado
                        const result = await admin.messaging().sendMulticast(message);
                        successCount += result.successCount;
                        failureCount += result.failureCount;
                        
                        // Identificar tokens inválidos para limpiarlos
                        if (result.failureCount > 0) {
                            const invalidTokensFromResponse = [];
                            result.responses.forEach((resp, idx) => {
                                if (!resp.success && (
                                    resp.error.code === 'messaging/invalid-registration-token' ||
                                    resp.error.code === 'messaging/registration-token-not-registered')) {
                                    invalidTokensFromResponse.push(tokensChunk[idx]);
                                }
                            });
                            invalidTokens = [...invalidTokens, ...invalidTokensFromResponse];
                        }
                    } catch (error) {
                        console.error('Error sending multicast message:', error);
                        failureCount += tokensChunk.length;
                    }
                }
                
                // Limpiar tokens inválidos de la base de datos
                if (invalidTokens.length > 0) {
                    await removeInvalidTokens(invalidTokens);
                }
                
                // Actualizar estado de la notificación
                const status = failureCount === tokens.length ? 'failed' : 
                              (failureCount > 0 ? 'partial' : 'success');
                              
                await updateNotificationStatus(snap.ref, status, {
                    successCount,
                    failureCount,
                    invalidTokensRemoved: invalidTokens.length
                });
                
                console.log(`Successfully sent notifications to ${successCount} devices`);
                if (failureCount > 0) {
                    console.log(`Failed to send notifications to ${failureCount} devices`);
                }
                
                return { success: true, successCount, failureCount };
            } 
            // Si es para un usuario específico
            else if (notification.targetUserId) {
                const userId = notification.targetUserId;
                console.log(`Sending notification to user: ${userId}`);
                
                // Intentar obtener el token más reciente del usuario
                const userTokensSnapshot = await admin.firestore()
                    .collection('fcm_tokens')
                    .where('userId', '==', userId)
                    .orderBy('timestamp', 'desc')
                    .limit(1)
                    .get();
                
                if (userTokensSnapshot.empty) {
                    await updateNotificationStatus(snap.ref, 'failed', 'No FCM token found for user');
                    console.log('User has no FCM token');
                    return { success: false, error: 'No FCM token found for user' };
                }
                
                const token = userTokensSnapshot.docs[0].data().token;
                
                // Preparar mensaje con alta prioridad
                const message = {
                    notification: { 
                        title, 
                        body,
                        sound: 'default'
                    },
                    data: messageData,
                    android: {
                        priority: 'high',
                        notification: {
                            sound: 'default',
                            channelId: 'SystemBooksChannel',
                            priority: 'high'
                        }
                    },
                    apns: {
                        payload: {
                            aps: {
                                sound: 'default',
                                contentAvailable: true
                            }
                        },
                        headers: {
                            'apns-push-type': 'alert',
                            'apns-priority': '10'
                        }
                    },
                    token: token
                };
                
                try {
                    // Enviar la notificación
                    const result = await admin.messaging().send(message);
                    console.log('Successfully sent message:', result);
                    
                    // Actualizar estado de la notificación
                    await updateNotificationStatus(snap.ref, 'success', { messageId: result });
                    
                    return { success: true };
                } catch (error) {
                    console.error('Error sending message:', error);
                    
                    // Si el token no es válido, eliminarlo
                    if (error.code === 'messaging/invalid-registration-token' ||
                        error.code === 'messaging/registration-token-not-registered') {
                        await removeTokenForUser(userId, token);
                    }
                    
                    // Actualizar estado de la notificación
                    await updateNotificationStatus(snap.ref, 'failed', error.message);
                    
                    return { success: false, error: error.message };
                }
            }
            
            await updateNotificationStatus(snap.ref, 'failed', 'Invalid notification type');
            return { success: false, error: 'Invalid notification type' };
        } catch (error) {
            console.error('Error sending notification:', error);
            
            // Intentar actualizar el estado de la notificación
            try {
                await snap.ref.update({
                    status: 'failed',
                    error: error.message,
                    processedAt: admin.firestore.FieldValue.serverTimestamp()
                });
            } catch (updateError) {
                console.error('Error updating notification status:', updateError);
            }
            
            return { success: false, error: error.message };
        }
    });

// Función para dividir un array en chunks
function chunkArray(array, size) {
    const chunks = [];
    for (let i = 0; i < array.length; i += size) {
        chunks.push(array.slice(i, i + size));
    }
    return chunks;
}

// Función para actualizar el estado de una notificación
async function updateNotificationStatus(docRef, status, details) {
    try {
        await docRef.update({
            status: status,
            statusDetails: details,
            processedAt: admin.firestore.FieldValue.serverTimestamp()
        });
    } catch (error) {
        console.error('Error updating notification status:', error);
    }
}

// Función para eliminar tokens inválidos
async function removeInvalidTokens(invalidTokens) {
    const db = admin.firestore();
    const batch = db.batch();
    let count = 0;
    
    try {
        // Buscar y eliminar los tokens inválidos
        const snapshot = await db.collection('fcm_tokens')
            .where('token', 'in', invalidTokens)
            .get();
            
        snapshot.forEach(doc => {
            batch.delete(doc.ref);
            count++;
        });
        
        // También buscar y actualizar los documentos de usuario que tengan estos tokens
        const usersSnapshot = await db.collection('users')
            .where('fcmToken', 'in', invalidTokens)
            .get();
            
        usersSnapshot.forEach(doc => {
            batch.update(doc.ref, { fcmToken: null });
        });
        
        await batch.commit();
        console.log(`Removed ${count} invalid tokens`);
    } catch (error) {
        console.error('Error removing invalid tokens:', error);
    }
}

// Función para eliminar token específico de un usuario
async function removeTokenForUser(userId, token) {
    const db = admin.firestore();
    
    try {
        // Eliminar de la colección de tokens
        const tokenDoc = await db.collection('fcm_tokens').doc(userId).get();
        if (tokenDoc.exists && tokenDoc.data().token === token) {
            await tokenDoc.ref.delete();
        }
        
        // Actualizar documento de usuario
        const userDoc = await db.collection('users').doc(userId).get();
        if (userDoc.exists && userDoc.data().fcmToken === token) {
            await userDoc.ref.update({ fcmToken: null });
        }
        
        console.log(`Removed invalid token for user ${userId}`);
    } catch (error) {
        console.error('Error removing invalid token for user:', error);
    }
}
