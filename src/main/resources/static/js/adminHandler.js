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


    editOrderModal.addEventListener('show.bs.modal', (event) => {
        // Получаем ID ордера из строки таблицы
        const triggerElement = event.relatedTarget.closest('tr'); // элемент <tr>, открывший модальное окно
        const orderId = triggerElement.getAttribute('data-order-id');

        // Устанавливаем ID в скрытое поле
        orderIdInput.value = orderId;
        orderIdToHeader.textContent = orderId;
        // Выполняем запрос к серверу
        fetch(`/order/get-order?id=${orderId}`)
            .then(response => {
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
    });
});


// AJAX список ордеров
document.addEventListener('DOMContentLoaded', function() {

    // Получаем CSRF токен из meta-тегов
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    const blockStatusInfo = document.getElementById('block-status-info');
    const editButton = document.getElementById('editStatusButton');
    const cancelButton = document.getElementById('cancelEditStatus');
    const form = document.getElementById('setStatus');

    const tableHeaders = document.querySelectorAll('.sortable');
    let currentSortField = null;
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

    // Функция загрузки данных с сортировкой
    function loadOrders(sortField = '', ascending = false) {
        const queryParams = new URLSearchParams({
            sortBy: sortField,
            ascending: ascending,
            type: 'bonus'
        });

        fetch(`/order/orders?${queryParams.toString()}`)
            .then(response => {
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

            // Предотвращаем стандартное поведение (перезагрузку страницы)
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
            fetch(`/order/get-order?id=${orderId}`) // URL для получения деталей ордера
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Ошибка при загрузке деталей ордера');
                    }
                    return response.json();
                })
                .then(orderDetails => {
                    // Заполняем модальное окно данными ордера
                    document.getElementById('order-id').textContent = orderDetails.id;
                    document.getElementById('order-date').textContent = orderDetails.createDate;
                    document.getElementById('orderStatus').textContent = orderDetails.statusOrdersDto.name; //Статус в форме выбора
                    document.getElementById('order-status').classList.add(orderDetails.statusOrdersDto.color)
                    document.getElementById('order-status').textContent = orderDetails.statusOrdersDto.name; // Статус в описании
                    document.getElementById('orderClient').textContent = orderDetails.userDto.fio;
                    document.getElementById('managerClient').textContent = orderDetails.userDto.managerDto.fio;
                    document.getElementById('invoice-number').textContent = orderDetails.invoiceNumber;
                    document.getElementById('invoice-date').textContent = orderDetails.invoiceDate;
                    document.getElementById('invoice-quantity-material').textContent = orderDetails.productQuantity;
                    document.getElementById('invoice-name-material').textContent = orderDetails.productName;
                    document.getElementById('invoice-sum').textContent = orderDetails.sumByInvoice;
                    document.getElementById('bonus-bid').textContent = orderDetails.bonusValueDto.value;
                    document.getElementById('bonus-value').textContent = orderDetails.sum;

                })
                .catch(error => console.error('Ошибка при загрузке деталей ордера:', error));
        }

//       function loadActions() {
//            let actionsTable = document.getElementById('managerActionsTable');
//            if (!actionsTable) {
//                console.warn("Элемент 'actionsTable' не найден!");
//                return;
//            }
//            fetch('/order/get-actions')
//                .then(response => response.json())
//                .then(data => {
//
//                    actionsTable.innerHTML = ''; // Очистка таблицы перед добавлением новых данных
//
//                    if (data.length > 0) {
//                        data.forEach(order => {
//                            let row = document.createElement('tr');
//                            // Создание элемента <span> с классом из order.status.color
//                            let statusSpan = `<span class="badge ${order.statusColor}">${order.statusName}</span>`;
//
//                            row.innerHTML = `
//                                <td>${order.createDate}</td>
//                                <td>${statusSpan}</td>
//                                <!--- <td>Счёт №${order.invoiceNumber} от ${order.invoiceDate}</td>
//                                <td>${order.productName}</td>
//                                <td>${order.productQuantity}</td> --->
//                                <td><a href="#" data-bs-toggle="modal" data-order-id=${order.id} data-bs-target="#viewUsersOrdersDetails">Детали</a></td>
//                            `;
//                            actionsTable.appendChild(row);
//                        });
//                    } else {
//                        actionsTable.innerHTML = '<tr><td colspan="3">Нет заказов для отображения.</td></tr>';
//                    }
//                })
//                .catch(error => console.error('Ошибка при загрузке заказов:', error));
//       }

       // Get ORDER DATA BY ID
//       function loadOrderById() {
//
//           if (event.target.tagName === 'A' && event.target.hasAttribute('data-order-id')) {
//               let orderId = event.target.getAttribute('data-order-id');
//
//                const request = "/order/get-order-by-manager?id=" + orderId;
//
//               fetch(request)
//                   .then(response => response.json())
//                   .then(order => {
//
//                   // Находим контейнер внутри модального окна, куда будем вставлять данные
//                    let modalBody = document.querySelector('#viewUsersOrdersDetails .modal-body');
//
//                    // Очищаем старые данные
//                    modalBody.innerHTML = '';
//
//                // Если данные заказа есть
//                if (order) {
//                    let row = document.createElement('dl');
//                    let statusSpan = `<span class="badge ${order.statusColor}">${order.statusName}</span>`;
//
//                    // Формируем HTML для отображения данных
//                    row.innerHTML = `
//                        <dt>Время создания: </dt>
//                        <dd>${order.createDate}</dd>
//                        <dt>Счёт: </dt>
//                        <dd>${order.invoiceNumber} от ${order.invoiceDate}</dd>
//                        <dt>Название товара:</dt>
//                        <dd>${order.productName}</dd>
//                        <dt>Количество товара:</dt>
//                        <dd>${order.productQuantity}</dd>
//                        <dt>Статус:</dt>
//                        <dd>${statusSpan}</dd>
//                    `;
//
//                    // Вставляем данные в модальное окно
//                    modalBody.appendChild(row);
//                } else {
//                    modalBody.innerHTML = '<p>Заказ не найден.</p>';
//                }
//               })
//                .catch(error => console.error('Ошибка при загрузке заказов:', error));
//           }
//       }

        // Загружаем заказы через таймаут, чтобы убедиться, что HTML загружен
        setTimeout(loadOrders, 100); // 100 миллисекунд
        //setTimeout(loadActions, 200); // 100 миллисекунд

        // Также загружаем заказы при клике на вкладку "Home"
//        const ordersTab = document.getElementById('admin-order-tab');
//        ordersTab.addEventListener('click', loadOrders);

//        const actionsTab = document.getElementById('mng-action-tab');
//        actionsTab.addEventListener('click', loadActions);

        // Загрузка ордера по клику подробнее
//        const actionsTable = document.getElementById('managerActionsTable');
//        actionsTable.addEventListener('click', loadOrderById );
//
//        const ordersTable = document.getElementById('managerOrdersTable');
//        ordersTable.addEventListener('click', loadOrderById );

});

