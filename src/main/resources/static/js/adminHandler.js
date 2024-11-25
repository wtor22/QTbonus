document.addEventListener('DOMContentLoaded', () => {
    const editOrderModal = document.getElementById('editOrderModal');
    const orderIdInput = document.getElementById('orderInputId');
    const orderIdToHeader = document.getElementById('order-id');
    const orderDateToHeader = document.getElementById('order-date');
    const orderStatusInput = document.getElementById('orderStatus'); //Статус в форме выбора
    const orderStatus = document.getElementById('order-status'); // Статус в описании
    const orderSumInput = document.getElementById('orderSum');
    const orderClientInput = document.getElementById('orderClient');
    const orderManager = document.getElementById('managerClient');
    const invoiceNumber = document.getElementById('invoice-number');
    const invoiceDate = document.getElementById('invoice-date');
    const invoiceQuantityMaterial = document.getElementById('invoice-quantity-material');
    const invoiceNameMaterial = document.getElementById('invoice-name-material');
    const invoiceSum = document.getElementById('invoice-sum');
    const invoiceBonusBid = document.getElementById('bonus-bid');
    const invoiceBonusValue = document.getElementById('bonus-value');

    function clearModalFields() {
        orderDateToHeader.textContent = '';
        orderStatusInput.value = '';
        orderClientInput.textContent = '';
        orderManager.textContent = '';
        invoiceNumber.textContent = '';
        invoiceDate.textContent = '';
        invoiceNameMaterial.textContent = '';
        invoiceQuantityMaterial.textContent = '';
        invoiceSum.textContent = '';
        invoiceBonusBid.textContent = '';
        invoiceBonusValue.textContent = '';
        orderSumInput.value = '';
        orderStatus.className = 'badge';
        orderStatus.textContent = '';
    }

    function getPayments(orderId) {
        if (!orderId) {
            console.error('orderId отсутствует');
            alert('Не указан идентификатор ордера.');
            return Promise.reject('Идентификатор ордера отсутствует'); // Возвращаем отклонённый промис
        }

        return fetch(`/payment/get-payments?orderId=${orderId}`)
            .then(response => {
                if (response.status === 403) {
                    // Если сервер вернул статус 403, перенаправляем на страницу 403
                    window.location.href = '/403';
                    return;
                }
                if (!response.ok) {
                    throw new Error(`Ошибка загрузки данных: ${response.statusText}`);
                }
                console.log("PAYMENTS GET OK")
                return response.json().catch(error => {
                  throw new Error('Ошибка обработки JSON: ' + error.message);
               });
            })
            .catch(error => {
                console.error('Ошибка:', error);
                alert('Не удалось загрузить данные ордера.');
                throw error; // Пробрасываем ошибку дальше
            });
    }

    editOrderModal.addEventListener('show.bs.modal', (event) => {
        clearModalFields();
        // Получаем ID ордера из строки таблицы
        const triggerElement = event.relatedTarget.closest('tr'); // элемент <tr>, открывший модальное окно
        const orderTriggerId = triggerElement.getAttribute('data-order-id');
        loadOrderDetails(orderTriggerId);

    });

    // AJAX список ордеров
    // Получаем CSRF токен из meta-тегов
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    const blockStatusInfo = document.getElementById('block-status-info');
    const editButton = document.getElementById('editStatusButton');
    const addPaymentButton = document.getElementById('addPaymentsButton');
    const cancelButton = document.getElementById('cancelEditStatus');
    const canselPaymentButton = document.getElementById('cancelSetPayment');
    const form = document.getElementById('setStatus');
    const formAddedPayment = document.getElementById('setPayment');
    const editPaymentButton = document.getElementBiId('editPaymentButton');

    // Переменные фильтра
    const filterForm = document.getElementById('filterOrderForm');
    const fioInput = document.getElementById('filterFio');
    const fioManagerInput = document.getElementById('filterManager');
    const typeInput = document.querySelector('input[name="type"]');
    const statusInput = document.getElementById('filterStatus');
    const invoiceInput = document.getElementById('filterInvoice');
    const startDateInput = document.getElementById('filterStartDate');
    const endDateInput = document.getElementById('filterEndDate');

    const tableHeaders = document.querySelectorAll('.sortable');
    let currentSortField = '';
    let ascending = true;

    tableHeaders.forEach(header => {
        header.addEventListener('click', () => {
            const sortField = header.getAttribute('data-sort');

            // Определяем направление сортировки
            if (currentSortField === sortField) {
                ascending = !ascending; // Меняем направление, если сортируем по тому же полю
            } else {
                currentSortField = sortField;
                ascending = true; // По умолчанию сортируем по возрастанию
            }

            // Убираем классы сортировки со всех заголовков
            tableHeaders.forEach(th => th.classList.remove('asc', 'desc'));

            // Добавляем текущий класс сортировки
            header.classList.add(ascending ? 'asc' : 'desc');

            // Загружаем данные с сортировкой
            loadOrders(currentSortField, ascending);
        });
    });
    // Получит даты в фильтре по умолчанию ( две недели )
    function getDefaultDates() {
        const today = new Date();
        const weekAgo = new Date();
        weekAgo.setDate(today.getDate() - 14);
        const formatDate = date => date.toISOString().split('T')[0];
        return {
            startDate: formatDate(weekAgo),
            endDate: formatDate(today)
        };
    }

    // Функция загрузки данных с сортировкой
    function loadOrders(sortField = '', ascending = false) {

        const fio = fioInput.value.trim();
        const managerFio = fioManagerInput.value.trim();
        const type = typeInput ? typeInput.value : '';
        const statusId = statusInput.value;
        const invoice = invoiceInput.value;

        // Установить значения в HTML форму по умолчанию, если не заданы
        if(!startDateInput.value) startDateInput.value = getDefaultDates().startDate;
        if(!endDateInput.value) endDateInput.value = getDefaultDates().endDate;
        const startDate = startDateInput.value;
        const endDate = endDateInput.value;

        const queryParams = new URLSearchParams({
            sortBy: sortField,
            ascending: ascending,
            startDate: startDate,
            endDate: endDate
        });

        if (type) {
            queryParams.append('type', type);
        }
        if (fio) {
            queryParams.append('fio', fio);
        }
        if (managerFio) {
            queryParams.append('managerFio', managerFio);
        }
        if (statusId) queryParams.append('statusId', statusId);

        if (invoiceInput) queryParams.append('invoiceInput', invoiceInput);

        fetch(`/order/orders?${queryParams.toString()}`)
            .then(response => {
                if (response.status === 403) {
                    // Если сервер вернул статус 403, перенаправляем на страницу 403
                    window.location.href = '/403';
                    return;
                }
                if (!response.ok) {
                    throw new Error(`Ошибка загрузки данных: ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                updateOrdersTable(data);
            })
            .catch(error => {
                console.error('Ошибка:', error);
                alert('Не удалось загрузить заказы.');
            });
    }

    // Функция для обновления таблицы
    function updateOrdersTable(orders) {
        const tableBody = document.getElementById('adminOrdersTable');
        tableBody.innerHTML = ''; // Очищаем таблицу

        orders.forEach(order => {
            const row = document.createElement('tr');
            // Добавляем атрибуты для модального окна
            row.setAttribute('data-order-id', order.id);
            row.setAttribute('data-bs-toggle', 'modal');
            row.setAttribute('data-bs-target', '#editOrderModal');
            const statusSpan = `<span class="badge ${order.statusOrdersDto.color}">${order.statusOrdersDto.name}</span>`;
            const managerFio = order.userDto.managerDto ? order.userDto.managerDto.fio : "---";
            row.innerHTML = `
                <td>${order.id}</td>
                <td>${order.createDate}</td>
                <td>${order.userDto.fio}</td>
                <td>${order.sum}</td>
                <td>${statusSpan}</td>
                <td>${managerFio}</td>
            `;
            tableBody.appendChild(row);
        });
    }

    // Нажатие кнопки добавление выплаты
    addPaymentButton.addEventListener('click', () => {
        const statusId = document.getElementById('orderStatus').value;

        const paymentValueInput = document.getElementById('orderPayment');
        const datePaymentInput = document.getElementById('orderPaymentDate');
        paymentValueInput.value = document.getElementById('bonus-value').textContent;
        datePaymentInput.value = getDefaultDates().endDate; // Устанавливаю сегодняшнюю дату
        formAddedPayment.classList.toggle('d-none');
    });

    // Обработка добавления выплаты
    formAddedPayment.addEventListener('submit', () => {
        event.preventDefault();

        const orderIdMod = document.getElementById('order-id').textContent;
        const paymentValueInput = document.getElementById('orderPayment');
        const datePaymentInput = document.getElementById('orderPaymentDate');
        const commentToPaymentInput = document.getElementById('commentPayment')

        // Собираю BonusPayment
        const bonusPayment = {
            order: {
                id: orderIdMod
            },
            sum: paymentValueInput.value,
            datePayment: datePaymentInput.value,
            commentToPayment: commentToPaymentInput.value,
        };

        // Отправка данных через fetch (AJAX)
        fetch(formAddedPayment.action, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken // Добавляем CSRF-токен в заголовки
            },
            body: JSON.stringify(bonusPayment)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Ошибка при отправке данных');
        })
        .then(data => {
            // Обновляем интерфейс
            loadOrderDetails(orderIdMod);
            console.log("CALL FUNCTION UPDATE MODAL FIELD")
            formAddedPayment.classList.toggle('d-none');
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось обновить статус.');
        });
    });

    canselPaymentButton.addEventListener('click', () => {
        formAddedPayment.classList.toggle('d-none');
    });

    // Нажатие кнопки редактирования статуса
    editButton.addEventListener('click', () => {
        // Получаем список статусов
        fetch('/order/statuses')
            .then(response => response.json())
            .then(statuses => {
                const statusSelect = document.getElementById('orderStatus');
                // Очищаем существующие OPTION
                statusSelect.innerHTML = '';

                statuses.forEach(status => {
                    const option = document.createElement('option');
                    option.value = status.id;
                    option.textContent = status.name;
                    statusSelect.appendChild(option);
                });
            })
            .catch(error => console.error('Ошибка при загрузке статусов:', error));

            // Показываем форму
            form.style.display = form.style.display === 'none' ? 'block' : 'none';

            // Прячем кнопку, если форма видна
            blockStatusInfo.style.display = form.style.display === 'none' ? 'inline' : 'none';
    });

    cancelButton.addEventListener('click', () => {
        form.style.display = 'none';
        blockStatusInfo.style.display = 'block';
    });

    // Обработка отправки формы на изменение статуса
    form.addEventListener('submit', () => {
        event.preventDefault();

        const orderIdMod = document.getElementById('order-id').textContent;
        const statusId = document.getElementById('orderStatus').value;

        // Собираю Order
        const order = {
            id: orderIdMod,
            status: {
                id: statusId
            }
        };

        // Отправка данных через fetch (AJAX)
        fetch(form.action, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken // Добавляем CSRF-токен в заголовки
            },
            body: JSON.stringify(order)
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Ошибка при отправке данных');
        })
        .then(data => {
            // Обновляем интерфейс
            document.getElementById('order-status').textContent = data.statusOrdersDto.name;
            document.getElementById('order-status').className = `badge ${data.statusOrdersDto.color}`;
            form.style.display = 'none';
            blockStatusInfo.style.display = 'block';

            // Обновляю ряд со статусом в списке
            const row = document.querySelector(`tr[data-order-id="${orderIdMod}"]`);
            if (row) {
                const statusSpan = `<span class="badge ${data.statusOrdersDto.color}">${data.statusOrdersDto.name}</span>`;
                const managerFio = data.userDto.managerDto ? data.userDto.managerDto.fio : "---";
                row.innerHTML = `
                    <td>${data.id}</td>
                    <td>${data.createDate}</td>
                    <td>${data.userDto.fio}</td>
                    <td>${data.sum}</td>
                    <td>${statusSpan}</td>
                    <td>${managerFio}</td>
                `;
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось обновить статус.');
        });
    });

    function loadOrderDetails(orderId) {
    clearModalFields();
            const containerDataPayment = document.getElementById('payment-data');
            // Получаем выплаты
            getPayments(orderId)
                .then(payments => {
                    containerDataPayment.innerHTML = '';
                    // Прохожу по массиву платежей
                    payments.forEach(payment => {
                        // Создаем элемент p для каждой записи
                        const paymentElement = document.createElement('p');

                        // Добавляем информацию о платеже (сумма и дата)
                        paymentElement.innerHTML = `
                            <span style="display:none">${payment.id}</span>
                            <span>Сумма: ${payment.sum}</span>
                            <span>Дата: ${payment.datePayment}</span>
                            ${payment.commentToPayment ? `</br><span>Комментарий: ${payment.commentToPayment}</span>` : ''}
                            <button id="editPaymentButton" attr-id="${payment.id}" class="btn btn-link" style="font-weight: normal;">Изменить</button>
                        `;

                        // Добавляем элемент p в контейнер
                        containerDataPayment.appendChild(paymentElement);
                    });
                    if (payments.length > 0) {
                        containerDataPayment.style.display = 'block';
                    } else {
                        containerDataPayment.style.display = 'none';
                    }
                })
                .catch(error => {
                    console.error('Ошибка при обработке платежей:', error);
                });


            // Устанавливаем ID в скрытое поле
            orderIdInput.value = orderId;
            orderIdToHeader.textContent = orderId;
            // Выполняем запрос к серверу
            fetch(`/order/get-order?id=${orderId}`)
                .then(response => {
                    if (response.status === 403) {
                        // Если сервер вернул статус 403, перенаправляем на страницу 403
                        window.location.href = '/403';
                        return;
                    }
                    if (!response.ok) {
                        throw new Error(`Ошибка загрузки данных: ${response.statusText}`);
                    }
                    return response.json();
                })
                .then(orderData => {
                    // Заполняем данные в форме
                    orderDateToHeader.textContent = orderData.createDate;
                    orderStatusInput.value = orderData.statusOrdersDto.id;

                    orderClientInput.textContent = orderData.userDto.fio;
                    orderManager.textContent = orderData.userDto.managerDto.fio;
                    invoiceNumber.textContent = orderData.invoiceNumber;
                    invoiceDate.textContent = orderData.invoiceDate;
                    invoiceNameMaterial.textContent = orderData.productName;
                    invoiceQuantityMaterial.textContent = orderData.productQuantity;
                    invoiceSum.textContent = orderData.sumByInvoice;
                    invoiceBonusBid.textContent = orderData.bonusValueDto.value;
                    invoiceBonusValue.textContent = orderData.sum;
                    orderSumInput.value = orderData.sum;
                    orderStatus.textContent = orderData.statusOrdersDto.name;
                    orderStatus.classList.add(orderData.statusOrdersDto.color);
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                    alert('Не удалось загрузить данные ордера.');
                });
    }

    // Клик по кнопке применить фильтр
    filterForm.addEventListener('submit', function (event) {
        event.preventDefault();

        const fio = fioInput.value.trim();
        const type = typeInput.value;
        const statusId = statusInput.value;
        const managerFio = fioManagerInput.value.trim();
        const invoiceNumber = invoiceInput.value.trim();
        const startDate = startDateInput.value;
        const endDate = endDateInput.value;

        const queryParams = new URLSearchParams({
            sortBy: currentSortField,
            ascending: ascending
        });

        if (type) queryParams.append('type', type);
        if (fio) queryParams.append('fio', fio);
        if (managerFio) queryParams.append('managerFio', managerFio);
        if (statusId) queryParams.append('statusId', statusId);
        if (invoiceNumber) queryParams.append('invoiceNumber', invoiceNumber);
        if (startDate) queryParams.append('startDate', startDate);
        if (endDate) queryParams.append('endDate', endDate);

        fetch(`/order/orders?${queryParams.toString()}`)
            .then(response => {
                if (response.status === 403) {
                    // Если сервер вернул статус 403, перенаправляем на страницу 403
                    window.location.href = '/403';
                    return;
                }
                if (!response.ok) {
                    throw new Error(`Ошибка загрузки данных: ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                updateOrdersTable(data);
            })
            .catch(error => {
                console.error('Ошибка:', error);
                alert('Не удалось загрузить заказы.');
            });
    });
    // Загружаем заказы через таймаут, чтобы убедиться, что HTML загружен
    setTimeout(loadOrders, 100); // 100 миллисекунд

});

