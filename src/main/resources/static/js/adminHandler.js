// Получение ордера
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
// Событие на кнопке изменить статус (Открытие формы для установки статуса)
document.addEventListener('DOMContentLoaded', () => {
    const blockStatusInfo = document.getElementById('block-status-info');
    const editButton = document.getElementById('editStatusButton');
    const cancelButton = document.getElementById('cancelEditStatus');
    const form = document.getElementById('setStatus');


    // Получаем CSRF токен из meta-тегов
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    editButton.addEventListener('click', () => {
        // Показываем форму
        form.style.display = form.style.display === 'none' ? 'block' : 'none';

        // Прячем кнопку, если форма видна
        blockStatusInfo.style.display = form.style.display === 'none' ? 'inline' : 'none';
    });

    cancelButton.addEventListener('click', () => {
        form.style.display = 'none';
        blockStatusInfo.style.display = 'block';
    });

    // Обработка отправки формы
    submitButton.addEventListener('click', () => {

        const orderId = document.getElementById('order-id').value;
        const statusId = document.getElementById('orderStatus').value;
        console.log("ORDER ID " + orderId + " STATUS ID " + statusId)

        // Собираю Order
        const order = {
            id: orderId,
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
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось обновить статус.');
        });
    });
});

